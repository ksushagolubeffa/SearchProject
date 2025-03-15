package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WebCrawler {
    private static final String OUTPUT_FOLDER = "downloaded_pages";
    private static final String INDEX_FILE = "index.txt";

    private final Queue<String> urlQueue = new LinkedList<>();
    private final Set<String> visitedUrls = new HashSet<>();

    public static void main(String[] args) {
        List<String> seedUrls = Arrays.asList(
                "https://ru.wikipedia.org/wiki/Гарри_Поттер",
                "https://ru.wikipedia.org/wiki/Лили_Поттер",
                "https://ru.wikipedia.org/wiki/Джеймс_Поттер",
                "https://ru.wikipedia.org/wiki/Джоан_Роулинг",
                "https://ru.wikipedia.org/wiki/Хогвартс",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_философский_камень",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Тайная_комната",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_узник_Азкабана",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Кубок_огня",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Орден_Феникса",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Принц-полукровка",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Дары_Смерти",
                "https://ru.wikipedia.org/wiki/Гермиона_Грейнджер",
                "https://ru.wikipedia.org/wiki/Рон_Уизли",
                "https://ru.wikipedia.org/wiki/Альбус_Дамблдор",
                "https://ru.wikipedia.org/wiki/Северус_Снегг",
                "https://ru.wikipedia.org/wiki/Волан-де-Морт",
                "https://ru.wikipedia.org/wiki/Минерва_Макгонагалл",
                "https://ru.wikipedia.org/wiki/Рубеус_Хагрид",
                "https://ru.wikipedia.org/wiki/Драко_Малфой",
                "https://ru.wikipedia.org/wiki/Сириус_Блэк",
                "https://ru.wikipedia.org/wiki/Люциус_Малфой",
                "https://ru.wikipedia.org/wiki/Беллатриса_Лестрейндж",
                "https://ru.wikipedia.org/wiki/Невилл_Долгопупс",
                "https://ru.wikipedia.org/wiki/Луна_Лавгуд",
                "https://ru.wikipedia.org/wiki/Джинни_Уизли",
                "https://ru.wikipedia.org/wiki/Фред_и_Джордж_Уизли",
                "https://ru.wikipedia.org/wiki/Артур_Уизли",
                "https://ru.wikipedia.org/wiki/Молли_Уизли",
                "https://ru.wikipedia.org/wiki/Чжоу_Чанг",
                "https://ru.wikipedia.org/wiki/Седрик_Диггори",
                "https://ru.wikipedia.org/wiki/Виктор_Крам",
                "https://ru.wikipedia.org/wiki/Флёр_Делакур",
                "https://ru.wikipedia.org/wiki/Аластор_Грюм",
                "https://ru.wikipedia.org/wiki/Долорес_Амбридж",
                "https://ru.wikipedia.org/wiki/Корнелиус_Фадж",
                "https://ru.wikipedia.org/wiki/Рита_Скитер",
                "https://ru.wikipedia.org/wiki/Питер_Петтигрю",
                "https://ru.wikipedia.org/wiki/Златопуст_Локонс",
                "https://ru.wikipedia.org/wiki/Помона_Стебль",
                "https://ru.wikipedia.org/wiki/Сивилла_Трелони",
                "https://ru.wikipedia.org/wiki/Филиус_Флитвик",
                "https://ru.wikipedia.org/wiki/Аргус_Филч",
                "https://ru.wikipedia.org/wiki/Оливер_Вуд",
                "https://ru.wikipedia.org/wiki/Кэти_Белл",
                "https://ru.wikipedia.org/wiki/Анджелина_Джонсон",
                "https://ru.wikipedia.org/wiki/Ли_Джордан",
                "https://ru.wikipedia.org/wiki/Пожиратели_смерти",
                "https://ru.wikipedia.org/wiki/Орден_Феникса",
                "https://ru.wikipedia.org/wiki/Квиддич",
                "https://ru.wikipedia.org/wiki/Фантастические_звери_и_места_их_обитания",
                "https://ru.wikipedia.org/wiki/Сказки_Барда_Бидля",
                "https://ru.wikipedia.org/wiki/Квиддич_с_древности_до_наших_дней",
                "https://ru.wikipedia.org/wiki/Дары_Смерти",
                "https://ru.wikipedia.org/wiki/Философский_камень",
                "https://ru.wikipedia.org/wiki/Кубок_огня",
                "https://ru.wikipedia.org/wiki/Хогсмид",
                "https://ru.wikipedia.org/wiki/Министерство_магии",
                "https://ru.wikipedia.org/wiki/Азкабан",
                "https://ru.wikipedia.org/wiki/Мародёры",
                "https://ru.wikipedia.org/wiki/Патронус",
                "https://ru.wikipedia.org/wiki/Анимаг",
                "https://ru.wikipedia.org/wiki/Крестраж",
                "https://ru.wikipedia.org/wiki/Волшебная_палочка",
                "https://ru.wikipedia.org/wiki/Мантия-невидимка",
                "https://ru.wikipedia.org/wiki/Карта_мародёров",
                "https://ru.wikipedia.org/wiki/Слизерин",
                "https://ru.wikipedia.org/wiki/Гриффиндор",
                "https://ru.wikipedia.org/wiki/Когтевран",
                "https://ru.wikipedia.org/wiki/Пуффендуй",
                "https://ru.wikipedia.org/wiki/Список_персонажей_серии_романов_о_Гарри_Поттере",
                "https://ru.wikipedia.org/wiki/Фантастические_твари:_Преступления_Грин-де-Вальда",
                "https://ru.wikipedia.org/wiki/Фантастические_твари:_Тайны_Дамблдора",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_(серия_фильмов)",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_философский_камень_(фильм)",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Тайная_комната_(фильм)",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_узник_Азкабана_(фильм)",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Кубок_огня_(фильм)",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Орден_Феникса_(фильм)",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Принц-полукровка_(фильм)",
                "https://ru.wikipedia.org/wiki/Гарри_Поттер_и_Дары_Смерти:_Часть_1",
                "https://ru.wikipedia.org/wiki/Акромантул",
                "https://ru.wikipedia.org/wiki/Боггарт",
                "https://ru.wikipedia.org/wiki/Пикси",
                "https://ru.wikipedia.org/wiki/Феникс",
                "https://ru.wikipedia.org/wiki/Вейла",
                "https://ru.wikipedia.org/wiki/Гиппогриф",
                "https://ru.wikipedia.org/wiki/Гоблин",
                "https://ru.wikipedia.org/wiki/Гриндилоу",
                "https://ru.wikipedia.org/wiki/Дементор",
                "https://ru.wikipedia.org/wiki/Домовой_эльф",
                "https://ru.wikipedia.org/wiki/Дракон",
                "https://ru.wikipedia.org/wiki/Единорог",
                "https://ru.wikipedia.org/wiki/Живоглот",
                "https://ru.wikipedia.org/wiki/Кентавр",
                "https://ru.wikipedia.org/wiki/Кикимора",
                "https://ru.wikipedia.org/wiki/Леший",
                "https://ru.wikipedia.org/wiki/Мандрагора",
                "https://ru.wikipedia.org/wiki/Нюхлер",
                "https://ru.wikipedia.org/wiki/Фестрал"
        );

        WebCrawler crawler = new WebCrawler();
        crawler.startCrawling(seedUrls);
        createArchive("downloaded_pages");
    }

    public void startCrawling(List<String> seedUrls) {
        urlQueue.addAll(seedUrls);
        int pageCounter = 1;

        createOutputFolder();

        try (BufferedWriter indexWriter = new BufferedWriter(new FileWriter(INDEX_FILE))) {
            while (!urlQueue.isEmpty()) {
                String url = urlQueue.poll();
                if (url == null || visitedUrls.contains(url)) continue;

                try {
                    Document doc = Jsoup.connect(url).get();
                    savePageToFile(doc, pageCounter, url);
                    indexWriter.write(pageCounter + " " + url);
                    indexWriter.newLine();

                    visitedUrls.add(url);
                    pageCounter++;

                } catch (IOException e) {
                    System.err.println("Ошибка при обработке: " + url);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка записи в index.txt: " + e.getMessage());
        }

        System.out.println("Скачано страниц: " + pageCounter);
    }

    private void savePageToFile(Document doc, int pageNumber, String url) throws IOException {
        File file = new File(OUTPUT_FOLDER, pageNumber + ".html");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(doc.html());
        }
        System.out.println("Сохранено: " + url + " -> " + file.getAbsolutePath());
    }

    private void createOutputFolder() {
        File folder = new File(OUTPUT_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void createArchive(String outputFolder) {
        try {
            // Указываем файл архива
            File zipFile = new File(outputFolder + ".zip");

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                File folder = new File(outputFolder);
                // Получаем список всех файлов в папке
                File[] files = folder.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            // Для каждого файла добавляем его в архив
                            try (FileInputStream fis = new FileInputStream(file)) {
                                ZipEntry zipEntry = new ZipEntry(file.getName());
                                zos.putNextEntry(zipEntry);

                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = fis.read(buffer)) >= 0) {
                                    zos.write(buffer, 0, length);
                                }
                                zos.closeEntry();
                            }
                        }
                    }
                }
                System.out.println("Архив создан: " + zipFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Ошибка при создании архива: " + e.getMessage());
        }
    }
}
