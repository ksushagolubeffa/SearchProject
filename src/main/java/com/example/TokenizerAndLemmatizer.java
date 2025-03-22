package com.example;


import com.github.demidko.aot.WordformMeaning;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TokenizerAndLemmatizer {
    private static final String INPUT_FOLDER = "downloaded_pages";
    private static final String OUTPUT_FOLDER = "processed_data";
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "и", "в", "во", "не", "что", "он", "на", "я", "с", "со", "как", "а", "то", "все", "она", "так", "его",
            "но", "да", "ты", "к", "у", "же", "вы", "за", "бы", "по", "только", "ее", "мне", "было", "вот", "от",
            "меня", "еще", "нет", "о", "из", "ему", "теперь", "когда", "даже", "ну", "вдруг", "ли", "если", "уже",
            "или", "ни", "быть", "был", "него", "до", "вас", "нибудь", "опять", "уж", "вам", "ведь", "там", "потом",
            "себя", "ничего", "ей", "может", "они", "тут", "где", "есть", "надо", "ней", "для", "мы", "тебя", "их",
            "чем", "была", "сам", "чтоб", "без", "будто", "чего", "раз", "тоже", "себе", "под", "будет", "ж", "тогда",
            "кто", "этот", "того", "потому", "этого", "какой", "совсем", "ним", "здесь", "эти", "хотя", "всегда",
            "конечно", "всю", "между"
    ));

    public static void main(String[] args) throws IOException {
        File folder = new File(INPUT_FOLDER);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".html"));

        if (files == null) {
            System.err.println("Файлы не найдены!");
            return;
        }

        new File(OUTPUT_FOLDER).mkdirs();

        for (File file : files) {
            String fileName = file.getName().replace(".html", "");

            Set<String> tokens = extractTokens(file);
            saveTokens(tokens, OUTPUT_FOLDER + "/tokens_" + fileName + ".txt");

            Map<String, Set<String>> lemmas = lemmatizeTokens(tokens);
            saveLemmas(lemmas, OUTPUT_FOLDER + "/lemmas_" + fileName + ".txt");

            System.out.println("Обработано: " + fileName);
        }

        System.out.println("✅ Готово! Файлы сохранены в папке " + OUTPUT_FOLDER);
    }

    private static Set<String> extractTokens(File file) throws IOException {
        Set<String> tokens = new HashSet<>();
        Pattern pattern = Pattern.compile("^[а-яА-Я]+$");

        Document doc = Jsoup.parse(file, "UTF-8");
        String text = doc.text().toLowerCase();

        String[] words = text.split("[\\s.,!?;:\\-()\\[\\]{}<>\"'«»]+");

        for (String word : words) {
            if (pattern.matcher(word).matches() && !STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }

        return tokens;
    }

    private static void saveTokens(Set<String> tokens, String filePath) throws IOException {
        List<String> sortedTokens = new ArrayList<>(tokens);
        Collections.sort(sortedTokens);
        Files.write(Paths.get(filePath), sortedTokens);
    }

    private static Map<String, Set<String>> lemmatizeTokens(Set<String> tokens) {
        Map<String, Set<String>> lemmaMap = new HashMap<>();

        for (String token : tokens) {
            String lemma = getMainLemma(token);
            lemmaMap.computeIfAbsent(lemma, k -> new HashSet<>()).add(token);
        }

        return lemmaMap;
    }

    private static String getMainLemma(String token) {
        return WordformMeaning.lookupForMeanings(token)
                .stream()
                .map(meaning -> meaning.getLemma().toString())
                .findFirst()
                .orElse(token);
    }

    private static void saveLemmas(Map<String, Set<String>> lemmas, String filePath) throws IOException {
        List<String> lines = lemmas.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.join(" ", entry.getValue()))
                .sorted()
                .collect(Collectors.toList());

        Files.write(Paths.get(filePath), lines);
    }
}
