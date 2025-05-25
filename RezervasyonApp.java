import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

class Ucak {
    String model, marka, seriNo;
    int kapasite;

    public Ucak(String model, String marka, String seriNo, int kapasite) {
        this.model = model;
        this.marka = marka;
        this.seriNo = seriNo;
        this.kapasite = kapasite;
    }

    public String toString() {
        return model + "," + marka + "," + seriNo + "," + kapasite;
    }

    public static Ucak fromString(String line) {
        String[] p = line.split(",");
        return new Ucak(p[0], p[1], p[2], Integer.parseInt(p[3]));
    }
}

class Lokasyon {
    String ulke, sehir, havaalani;
    boolean aktif;

    public Lokasyon(String ulke, String sehir, String havaalani, boolean aktif) {
        this.ulke = ulke;
        this.sehir = sehir;
        this.havaalani = havaalani;
        this.aktif = aktif;
    }

    public String toString() {
        return ulke + "," + sehir + "," + havaalani + "," + aktif;
    }

    public static Lokasyon fromString(String line) {
        String[] p = line.split(",");
        return new Lokasyon(p[0], p[1], p[2], Boolean.parseBoolean(p[3]));
    }
}

class Ucus {
    static int sayac = 0;
    int id;
    Lokasyon lokasyon;
    String saat;
    Ucak ucak;

    public Ucus(Lokasyon lokasyon, String saat, Ucak ucak) {
        this.id = ++sayac;
        this.lokasyon = lokasyon;
        this.saat = saat;
        this.ucak = ucak;
    }

    public Ucus(int id, Lokasyon lokasyon, String saat, Ucak ucak) {
        this.id = id;
        this.lokasyon = lokasyon;
        this.saat = saat;
        this.ucak = ucak;
        if (id > sayac) sayac = id;
    }

    public String toString() {
        return id + "," + lokasyon.sehir + "," + saat + "," + ucak.model;
    }

    public static Ucus fromString(String line, List<Lokasyon> lokasyonlar, List<Ucak> ucaklar) {
        String[] p = line.split(",");
        int id = Integer.parseInt(p[0]);
        String sehir = p[1];
        String saat = p[2];
        String model = p[3];
        Lokasyon l = lokasyonlar.stream().filter(x -> x.sehir.equals(sehir)).findFirst().orElse(null);
        Ucak u = ucaklar.stream().filter(x -> x.model.equals(model)).findFirst().orElse(null);
        if (l != null && u != null) {
            return new Ucus(id, l, saat, u);
        }
        return null;
    }
}

class Rezervasyon {
    Ucus ucus;
    String ad, soyad;
    int yas;

    public Rezervasyon(Ucus ucus, String ad, String soyad, int yas) {
        this.ucus = ucus;
        this.ad = ad;
        this.soyad = soyad;
        this.yas = yas;
    }

    public String toString() {
        return ucus.id + "," + ad + "," + soyad + "," + yas;
    }

    public static Rezervasyon fromString(String line, List<Ucus> ucuslar) {
        String[] p = line.split(",");
        int ucusId = Integer.parseInt(p[0]);
        for (Ucus u : ucuslar) {
            if (u.id == ucusId) {
                return new Rezervasyon(u, p[1], p[2], Integer.parseInt(p[3]));
            }
        }
        return null;
    }
}

public class RezervasyonApp {
    static List<Ucak> ucaklar = new ArrayList<>();
    static List<Lokasyon> lokasyonlar = new ArrayList<>();
    static List<Ucus> ucuslar = new ArrayList<>();
    static List<Rezervasyon> rezervasyonlar = new ArrayList<>();

    static String dataFolder;

    public static void main(String[] args) throws IOException {
        String rootFolder = findUcusRezervasyonFolder();
        if (rootFolder == null) {
            System.out.println("UçuşRezervasyon klasörü bulunamadı! Program data klasörünü oluşturamayacak.");
            return;
        }
        dataFolder = rootFolder + File.separator + "data" + File.separator;
        new File(dataFolder).mkdirs();

        loadAllData();

        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Uçak Bilet Rezervasyon Sistemi ---");
            System.out.println("1- Uçak Ekle\n2- Lokasyon Ekle\n3- Uçuş Ekle\n4- Rezervasyon Yap\n5- Rezervasyonları Görüntüle\n6- Çıkış");
            System.out.print("Seçiminiz: ");
            int secim = input.nextInt(); input.nextLine();

            switch (secim) {
                case 1:
                    System.out.print("Model: "); String model = input.nextLine();
                    System.out.print("Marka: "); String marka = input.nextLine();
                    System.out.print("Seri No: "); String seri = input.nextLine();
                    System.out.print("Kapasite: "); int kapasite = input.nextInt(); input.nextLine();
                    ucaklar.add(new Ucak(model, marka, seri, kapasite));
                    saveToFile("ucaklar.csv", ucaklar);
                    System.out.println("Uçak başarıyla eklendi.");
                    break;
                case 2:
                    System.out.print("Ülke: "); String ulke = input.nextLine();
                    System.out.print("Şehir: "); String sehir = input.nextLine();
                    System.out.print("Havaalanı: "); String havaalani = input.nextLine();
                    System.out.print("Aktif mi? (true/false): "); boolean aktif = input.nextBoolean(); input.nextLine();
                    lokasyonlar.add(new Lokasyon(ulke, sehir, havaalani, aktif));
                    saveToFile("lokasyonlar.csv", lokasyonlar);
                    System.out.println("Lokasyon başarıyla eklendi.");
                    break;
                case 3:
                    if (lokasyonlar.isEmpty() || ucaklar.isEmpty()) {
                        System.out.println("Uçak veya lokasyon eksik.");
                        break;
                    }
                    System.out.println("Lokasyonlar:");
                    for (int i = 0; i < lokasyonlar.size(); i++)
                        System.out.println(i + " - " + lokasyonlar.get(i).sehir);
                    int lid = input.nextInt(); input.nextLine();
                    System.out.print("Saat (örn: 10:00): "); String saat = input.nextLine();
                    System.out.println("Uçaklar:");
                    for (int i = 0; i < ucaklar.size(); i++)
                        System.out.println(i + " - " + ucaklar.get(i).model);
                    int uid = input.nextInt(); input.nextLine();
                    ucuslar.add(new Ucus(lokasyonlar.get(lid), saat, ucaklar.get(uid)));
                    saveToFile("ucuslar.csv", ucuslar);
                    System.out.println("Uçuş başarıyla eklendi.");
                    break;
                case 4:
                    if (ucuslar.isEmpty()) {
                        System.out.println("Uçuş bulunamadı.");
                        break;
                    }
                    System.out.println("Uçuşlar:");
                    for (int i = 0; i < ucuslar.size(); i++)
                        System.out.println(i + " - " + ucuslar.get(i));
                    int uidx = input.nextInt(); input.nextLine();
                    long rezervasyonSayisi = rezervasyonlar.stream()
                        .filter(r -> r.ucus == ucuslar.get(uidx)).count();
                    if (rezervasyonSayisi >= ucuslar.get(uidx).ucak.kapasite) {
                        System.out.println("Uçakta boş koltuk yok.");
                        break;
                    }
                    System.out.print("Ad: "); String ad = input.nextLine();
                    System.out.print("Soyad: "); String soyad = input.nextLine();
                    System.out.print("Yaş: "); int yas = input.nextInt(); input.nextLine();
                    rezervasyonlar.add(new Rezervasyon(ucuslar.get(uidx), ad, soyad, yas));
                    saveToFile("rezervasyonlar.csv", rezervasyonlar);
                    System.out.println("Rezervasyon başarıyla yapıldı.");
                    break;
                case 5:
                    if (rezervasyonlar.isEmpty()) {
                        System.out.println("Henüz rezervasyon yapılmadı.");
                    } else {
                        System.out.println("--- Rezervasyonlar ---");
                        for (Rezervasyon r : rezervasyonlar)
                            System.out.println("Uçuş: " + r.ucus + " | Ad: " + r.ad + " " + r.soyad + " | Yaş: " + r.yas);
                    }
                    break;
                case 6:
                    System.out.println("Veriler kaydedildi. Çıkılıyor...");
                    return;
                default:
                    System.out.println("Geçersiz seçim.");
            }
        }
    }

    static String findUcusRezervasyonFolder() {
        String currentPath = System.getProperty("user.dir");
        File current = new File(currentPath);

        while (current != null) {
            if (current.getName().equalsIgnoreCase("UcusRezervasyon")) { 
                return current.getAbsolutePath();
            }
            current = current.getParentFile();
        }
        return null;
    }

    static void loadAllData() throws IOException {
        ucaklar = load(dataFolder + "ucaklar.csv", Ucak::fromString);
        lokasyonlar = load(dataFolder + "lokasyonlar.csv", Lokasyon::fromString);
        ucuslar = load(dataFolder + "ucuslar.csv", line -> Ucus.fromString(line, lokasyonlar, ucaklar));
        rezervasyonlar = load(dataFolder + "rezervasyonlar.csv", line -> Rezervasyon.fromString(line, ucuslar));
    }

    static <T> void saveToFile(String fileName, List<T> list) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(dataFolder + fileName), StandardCharsets.UTF_8))) {
            for (T t : list) {
                bw.write(t.toString());
                bw.newLine();
            }
        }
    }

    static <T> List<T> load(String filePath, java.util.function.Function<String, T> parser) throws IOException {
        List<T> list = new ArrayList<>();
        File f = new File(filePath);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                T item = parser.apply(line);
                if (item != null)
                    list.add(item);
            }
        }
        return list;
    }
}
