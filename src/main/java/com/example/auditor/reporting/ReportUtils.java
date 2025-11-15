package com.example.auditor.reporting;

import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.FileIcon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportUtils {

    // --- Методы для экранирования ---
    public static String escapeMarkdown(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("`", "\\`")
                .replace("#", "\\#")
                .replace("<", "<")
                .replace(">", ">");
    }

    public static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "<")
                .replace(">", ">")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    public static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // --- Метод для чтения содержимого файла ---
    public static String readFileContent(Path filePath) throws IOException {
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    // --- Метод для генерации дерева файлов в формате Markdown ---
    public static String generateTreeMarkdown(List<FileInfo> files) {
        StringBuilder tree = new StringBuilder();
        List<String> sortedPaths = files.stream()
                .map(FileInfo::getRelativePath)
                .sorted()
                .collect(Collectors.toList());

        Set<String> addedDirs = new java.util.HashSet<>();
        Set<String> addedFiles = new java.util.HashSet<>();

        for (String path : sortedPaths) {
            String[] parts = path.split("/");
            StringBuilder currentPath = new StringBuilder();

            for (int i = 0; i < parts.length - 1; i++) {
                currentPath.append(parts[i]).append("/");
                String dirPath = currentPath.toString();
                if (!addedDirs.contains(dirPath)) {
                    String indent = "   ".repeat(i);
                    tree.append(indent).append("📁 ").append(parts[i]).append("\n");
                    addedDirs.add(dirPath);
                }
            }

            String fileName = parts[parts.length - 1];
            String parentDirPath = currentPath.toString();
            String fullPath = parentDirPath + fileName;
            if (!addedFiles.contains(fullPath)) {
                String indent = "   ".repeat(parts.length - 1);
                String icon = FileIcon.getIcon(fileName);
                tree.append(indent).append(icon).append(" ").append(fileName).append("\n");
                addedFiles.add(fullPath);
            }
        }
        return tree.toString();
    }

    // --- Метод для получения текущей даты ---
    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.getDefault()).format(new Date());
    }
}