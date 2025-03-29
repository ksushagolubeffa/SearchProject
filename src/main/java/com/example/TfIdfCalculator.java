package com.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class TfIdfCalculator {
    private static final String INPUT_FOLDER = "processed_data";
    private static final String OUTPUT_FOLDER = "tfidf_data";
    private final Map<String, Integer> docFrequency = new HashMap<>();
    private int totalDocuments;

    public static void main(String[] args) throws IOException {
        TfIdfCalculator calculator = new TfIdfCalculator();
        calculator.calculateTfIdf();
    }

    public void calculateTfIdf() throws IOException {
        File folder = new File(INPUT_FOLDER);
        File[] files = folder.listFiles((dir, name) -> name.startsWith("lemmas_") && name.endsWith(".txt"));
        if (files == null) {
            System.err.println("Лемматизированные файлы не найдены!");
            return;
        }

        totalDocuments = files.length;
        System.out.println("Общее количество документов: " + totalDocuments);

        // Подсчёт DF (количество документов с термином)
        for (File file : files) {
            Set<String> uniqueTerms = Files.lines(file.toPath())
                    .map(line -> line.split(":")[0])
                    .collect(Collectors.toSet());
            for (String term : uniqueTerms) {
                docFrequency.put(term, docFrequency.getOrDefault(term, 0) + 1);
            }
        }

        // Создание папки для хранения TF-IDF данных
        new File(OUTPUT_FOLDER).mkdirs();

        // Вычисление TF, IDF и TF-IDF для каждого файла
        for (File file : files) {
            Map<String, Double> tfMap = new HashMap<>();
            Map<String, Double> tfIdfMap = new HashMap<>();

            List<String> lines = Files.readAllLines(file.toPath());
            Map<String, Integer> termCount = new HashMap<>();
            int totalTerms = 0;

            // Подсчёт TF
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length < 2) continue;

                String lemma = parts[0].trim();
                String[] variants = parts[1].trim().split(" ");
                int count = variants.length;

                termCount.put(lemma, count);
                totalTerms += count;
            }

            // Расчёт TF и TF-IDF
            for (Map.Entry<String, Integer> entry : termCount.entrySet()) {
                String term = entry.getKey();
                int count = entry.getValue();

                double tf = (double) count / totalTerms;
                double idf = Math.log((double) totalDocuments / docFrequency.get(term));
                double tfIdf = tf * idf;

                tfMap.put(term, tf);
                tfIdfMap.put(term, tfIdf);
            }

            // Сохранение результата в файл
            saveTfIdfToFile(file.getName(), tfMap, tfIdfMap);
            System.out.println("✅ TF-IDF рассчитан для файла: " + file.getName());
        }
    }

    private void saveTfIdfToFile(String fileName, Map<String, Double> tfMap, Map<String, Double> tfIdfMap) throws IOException {
        String outputFileName = OUTPUT_FOLDER + "/" + fileName.replace("lemmas_", "tfidf_");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            for (String term : tfMap.keySet()) {
                double idf = Math.log((double) totalDocuments / docFrequency.get(term));
                double tfIdf = tfIdfMap.get(term);
                writer.write(term + " " + idf + " " + tfIdf);
                writer.newLine();
            }
        }
    }
}

