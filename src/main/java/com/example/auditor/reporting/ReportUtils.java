package com.example.auditor.reporting;

import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.FileIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportUtils.class);

    // --- НОВЫЙ МЕТОД: Проверка, находится ли путь внутри базового каталога ---
    public static boolean isPathInsideBaseDirectory(Path filePath, Path baseDirectoryPath) {
        try {
            // Получаем нормализованные абсолютные пути
            Path normalizedFilePath = filePath.normalize().toAbsolutePath();
            Path normalizedBasePath = baseDirectoryPath.normalize().toAbsolutePath();

            // Relativize пути
            Path relativePath = normalizedBasePath.relativize(normalizedFilePath);

            // Если relativize возвращает путь, начинающийся с "..", значит filePath вне baseDirectory
            // relativize возвращает пустой путь, если filePath == baseDirectory
            // relativize возвращает путь внутри, если filePath внутри baseDirectory
            // Проверяем только начало результата.
            return !relativePath.toString().startsWith("..");
        } catch (IllegalArgumentException e) {
            // relativize может выбросить IllegalArgumentException, если пути несовместимы (например, разные диски в Windows)
            LOGGER.warn("Не удалось определить, находится ли файл '{}' внутри базовой директории '{}': {}", filePath, baseDirectoryPath, e.getMessage());
            return false; // В случае ошибки считаем, что путь не внутри (для безопасности)
        }
    }
    // --- /НОВЫЙ МЕТОД ---

    // --- Метод для экранирования ---
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

    // --- Метод для чтения содержимого файла (с безопасностью) ---
    public static String readFileContent(Path filePath, Path baseDirectoryPath) throws IOException {
        // Проверяем, находится ли файл внутри разрешённой директории
        if (!isPathInsideBaseDirectory(filePath, baseDirectoryPath)) {
            LOGGER.warn("Попытка чтения файла за пределами базовой директории: {}. Файл будет пропущен.", filePath);
            return ""; // Возвращаем пустую строку, если путь вне разрешённой области
        }

        // Если проверка пройдена, читаем файл
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }
    // --- /Метод для чтения содержимого файла (с безопасностью) ---

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

            for (int i = 0; i < parts.length - 1; i++) { // -1, чтобы не включать файл
                currentPath.append(parts[i]).append("/");
                String dirPath = currentPath.toString();
                if (!addedDirs.contains(dirPath)) {
                    String indent = "   ".repeat(i); // Отступы
                    tree.append(indent).append("📁 ").append(parts[i]).append("\n");
                    addedDirs.add(dirPath);
                }
            }

            String fileName = parts[parts.length - 1];
            String parentDirPath = currentPath.toString();
            String fullPath = parentDirPath + fileName;
            if (!addedFiles.contains(fullPath)) {
                String indent = "   ".repeat(parts.length - 1); // Отступы для файла
                String icon = FileIcon.getIcon(fileName); // Иконка для файла
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