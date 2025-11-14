package com.example.auditor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GitIgnoreParser {

    public List<String> parseGitIgnore(String projectPath) {
        Path gitIgnorePath = Paths.get(projectPath, ".gitignore");
        if (!Files.exists(gitIgnorePath)) {
            return new ArrayList<>();
        }

        try {
            List<String> lines = Files.readAllLines(gitIgnorePath, java.nio.charset.StandardCharsets.UTF_8);
            List<String> patterns = new ArrayList<>();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                    continue;
                }

                // Конвертация .gitignore паттернов в glob
                String pattern = convertToGlobPattern(line);
                patterns.add(pattern);
            }

            System.out.println("Загружено " + patterns.size() + " правил из .gitignore");
            return patterns;

        } catch (IOException e) {
            System.err.println("Не удалось обработать .gitignore: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String convertToGlobPattern(String gitIgnorePattern) {
        // Убираем завершающий слеш для директорий
        boolean isDirectory = gitIgnorePattern.endsWith("/");
        if (isDirectory) {
            gitIgnorePattern = gitIgnorePattern.substring(0, gitIgnorePattern.length() - 1);
        }

        // Обработка паттернов
        if (gitIgnorePattern.startsWith("/")) {
            // Корневой паттерн
            gitIgnorePattern = "**" + gitIgnorePattern;
        } else if (gitIgnorePattern.contains("/")) {
            // Паттерн с путем
            gitIgnorePattern = "**/" + gitIgnorePattern;
        } else {
            // Простой паттерн файла
            gitIgnorePattern = "**/" + gitIgnorePattern;
        }

        // Добавляем звездочки для директорий
        if (isDirectory) {
            gitIgnorePattern += "/**";
        }

        return gitIgnorePattern;
    }
}