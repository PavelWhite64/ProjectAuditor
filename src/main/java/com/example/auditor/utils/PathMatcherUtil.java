// src/main/java/com/example/auditor/utils/PathMatcher.java
package com.example.auditor.utils;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

public class PathMatcherUtil { // Переименовал класс, чтобы избежать конфликта с java.nio.file.PathMatcher

    public static boolean matchFile(String filePath, List<String> patterns) {
        for (String pattern : patterns) {
            // Используем стандартный PathMatcher с glob
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            // Убедимся, что filePath использует '/' как разделитель, как ожидает PathMatcher
            if (pathMatcher.matches(Paths.get(filePath.replace('\\', '/')))) {
                return true;
            }
        }
        return false;
    }
}