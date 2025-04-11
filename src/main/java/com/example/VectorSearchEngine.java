package com.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class VectorSearchEngine {
    private static final String TFIDF_FOLDER = "tfidf_data";
    private final Map<String, Map<String, Double>> documentVectors = new HashMap<>();
    private final Set<String> vocabulary = new HashSet<>();

    public static void main(String[] args) throws IOException {
        VectorSearchEngine engine = new VectorSearchEngine();
        engine.loadTfIdfVectors();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите запрос:");
        String query = scanner.nextLine();

        List<String> results = engine.search(query);
        System.out.println("🔎 Результаты (по убыванию релевантности):");
        for (String result : results) {
            System.out.println("📄 " + result);
        }
    }

    public void loadTfIdfVectors() throws IOException {
        File folder = new File(TFIDF_FOLDER);
        File[] files = folder.listFiles((dir, name) -> name.startsWith("tfidf_") && name.endsWith(".txt"));

        if (files == null) {
            System.err.println("Файлы TF-IDF не найдены!");
            return;
        }

        for (File file : files) {
            Map<String, Double> vector = new HashMap<>();
            List<String> lines = Files.readAllLines(file.toPath());

            for (String line : lines) {
                String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    String term = parts[0];
                    double tfidf = Double.parseDouble(parts[2]);
                    vector.put(term, tfidf);
                    vocabulary.add(term);
                }
            }

            String docName = file.getName().replace("tfidf_", "").replace(".txt", "");
            documentVectors.put(docName, vector);
        }

        System.out.println("Загружено TF-IDF векторов: " + documentVectors.size());
    }

    public List<String> search(String query) {
        Map<String, Double> queryVector = buildQueryVector(query);

        Map<String, Double> similarities = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : documentVectors.entrySet()) {
            String docName = entry.getKey();
            double sim = cosineSimilarity(queryVector, entry.getValue());
            similarities.put(docName, sim);
        }

        return similarities.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, Double> buildQueryVector(String query) {
        String[] tokens = query.toLowerCase().split("\\s+");
        Map<String, Integer> termCount = new HashMap<>();
        for (String token : tokens) {
            if (vocabulary.contains(token)) {
                termCount.put(token, termCount.getOrDefault(token, 0) + 1);
            }
        }

        Map<String, Double> vector = new HashMap<>();
        int totalTerms = termCount.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : termCount.entrySet()) {
            double tf = (double) entry.getValue() / totalTerms;
            double idf = computeIdf(entry.getKey());
            vector.put(entry.getKey(), tf * idf);
        }

        return vector;
    }

    private double computeIdf(String term) {
        long docCount = documentVectors.values().stream()
                .filter(map -> map.containsKey(term))
                .count();
        if (docCount == 0) return 0;
        return Math.log((double) documentVectors.size() / docCount);
    }

    private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(v1.keySet());
        allKeys.addAll(v2.keySet());

        double dotProduct = 0.0, norm1 = 0.0, norm2 = 0.0;

        for (String key : allKeys) {
            double val1 = v1.getOrDefault(key, 0.0);
            double val2 = v2.getOrDefault(key, 0.0);
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }

        return (norm1 == 0 || norm2 == 0) ? 0 : dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
