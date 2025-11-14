package com.example.auditor.utils;

import java.util.List;
import java.util.regex.Pattern;

public class PathMatcher {

    public static boolean matchFile(String filePath, List<String> patterns) {
        String normalizedPath = filePath.replace("\\", "/");

        for (String pattern : patterns) {
            String normalizedPattern = pattern.replace("\\", "/");

            // Поддержка ** для любого уровня вложенности
            if (normalizedPattern.contains("**")) {
                String regexPattern = convertGlobToRegex(normalizedPattern);
                if (Pattern.matches(regexPattern, normalizedPath)) {
                    return true;
                }
            }
            // Простые wildcards
            else if (normalizedPattern.contains("*")) {
                String fileName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);
                if (fileName.matches(normalizedPattern.replace("*", ".*").replace("?", "."))) {
                    return true;
                }
            }
            // Точное совпадение
            else {
                if (normalizedPath.equals(normalizedPattern)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String convertGlobToRegex(String globPattern) {
        // Экранируем специальные символы
        String regex = Pattern.quote(globPattern);

        // Заменяем ** на .*
        regex = regex.replace("\\*\\*", ".*");

        // Заменяем * на [^/]* (любые символы кроме слеша)
        regex = regex.replace("\\*", "[^/]*");

        // Заменяем ? на .
        regex = regex.replace("\\?", ".");

        // Добавляем ^ и $ для полного совпадения
        return "^" + regex + "$";
    }
}