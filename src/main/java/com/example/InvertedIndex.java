package com.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class InvertedIndex {
    private static final String INPUT_FOLDER = "processed_data";
    private static final String INDEX_FILE = "inverted_index.txt";
    private final Map<String, Set<String>> index = new HashMap<>();

    public static void main(String[] args) throws IOException {
        InvertedIndex invertedIndex = new InvertedIndex();
        invertedIndex.buildIndex();
        invertedIndex.saveIndex();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите запрос (например, (цезарь AND клеопатра) OR (антоний AND NOT цицерон)):");
        String query = scanner.nextLine();
        Set<String> result = invertedIndex.booleanSearch(query);
        System.out.println("Результаты поиска: " + result);
    }

    public void buildIndex() throws IOException {
        File folder = new File(INPUT_FOLDER);
        File[] files = folder.listFiles((dir, name) -> name.startsWith("lemmas_") && name.endsWith(".txt"));

        if (files == null) {
            System.err.println("Лемматизированные файлы не найдены!");
            return;
        }

        for (File file : files) {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length < 2) continue;

                String lemma = parts[0].trim();
                String fileName = file.getName().replace("lemmas_", "").replace(".txt", "");

                index.computeIfAbsent(lemma, k -> new HashSet<>()).add(fileName);
            }
        }

        System.out.println("✅ Индекс построен.");
    }

    public void saveIndex() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INDEX_FILE))) {
            for (Map.Entry<String, Set<String>> entry : index.entrySet()) {
                writer.write(entry.getKey() + ": " + String.join(", ", entry.getValue()));
                writer.newLine();
            }
        }
        System.out.println("✅ Индекс сохранен в файл: " + INDEX_FILE);
    }

    public Set<String> booleanSearch(String query) {
        return evaluateExpression(tokenize(query));
    }

    private List<String> tokenize(String query) {
        return Arrays.asList(query.toLowerCase().split("\\s+"));
    }

    private Set<String> evaluateExpression(List<String> tokens) {
        Stack<Set<String>> operands = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            switch (token) {
                case "and":
                case "or":
                case "not":
                    operators.push(token);
                    break;
                case "(":
                    operators.push(token);
                    break;
                case ")":
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        processOperator(operands, operators.pop());
                    }
                    operators.pop();
                    break;
                default:
                    operands.push(index.getOrDefault(token, new HashSet<>()));
            }
        }

        while (!operators.isEmpty()) {
            processOperator(operands, operators.pop());
        }

        return operands.isEmpty() ? new HashSet<>() : operands.pop();
    }

    private void processOperator(Stack<Set<String>> operands, String operator) {
        Set<String> result;

        switch (operator) {
            case "not":
                result = negate(operands.pop());
                operands.push(result);
                break;
            case "and":
                Set<String> rightAnd = operands.pop();
                Set<String> leftAnd = operands.pop();
                leftAnd.retainAll(rightAnd);
                operands.push(leftAnd);
                break;
            case "or":
                Set<String> rightOr = operands.pop();
                Set<String> leftOr = operands.pop();
                leftOr.addAll(rightOr);
                operands.push(leftOr);
                break;
        }
    }

    private Set<String> negate(Set<String> set) {
        Set<String> allFiles = new HashSet<>(getAllFiles());
        allFiles.removeAll(set);
        return allFiles;
    }

    private Set<String> getAllFiles() {
        File folder = new File(INPUT_FOLDER);
        File[] files = folder.listFiles((dir, name) -> name.startsWith("lemmas_") && name.endsWith(".txt"));
        return files == null ? new HashSet<>() :
                Arrays.stream(files)
                        .map(file -> file.getName().replace("lemmas_", "").replace(".txt", ""))
                        .collect(Collectors.toSet());
    }
}

