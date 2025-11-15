package com.example.auditor.utils;

/**
 * Утилита для работы с расширениями файлов
 */
public class FileExtensionUtils {

    public enum ExtensionFormat {
        WITH_DOT,      // ".java"
        WITHOUT_DOT,   // "java"
        LOWERCASE      // "java" в нижнем регистре
    }

    private FileExtensionUtils() {
        // Утилитный класс - приватный конструктор
    }

    /**
     * Получает расширение файла в указанном формате
     */
    public static String getExtension(String fileName, ExtensionFormat format) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastDotIndex + 1);

            switch (format) {
                case WITH_DOT:
                    return "." + extension.toLowerCase();
                case WITHOUT_DOT:
                    return extension.toLowerCase();
                case LOWERCASE:
                    return extension.toLowerCase();
                default:
                    return extension;
            }
        }

        return "";
    }

    /**
     * Проверяет, имеет ли файл указанное расширение
     */
    public static boolean hasExtension(String fileName, String extension) {
        if (fileName == null || extension == null) {
            return false;
        }

        String fileExtension = getExtension(fileName, ExtensionFormat.WITHOUT_DOT);
        String targetExtension = extension.startsWith(".")
                ? extension.substring(1).toLowerCase()
                : extension.toLowerCase();

        return fileExtension.equals(targetExtension);
    }

    /**
     * Проверяет, имеет ли файл любое из указанных расширений
     */
    public static boolean hasAnyExtension(String fileName, java.util.Set<String> extensions) {
        if (fileName == null || extensions == null || extensions.isEmpty()) {
            return false;
        }

        String fileExtension = getExtension(fileName, ExtensionFormat.WITHOUT_DOT);
        return extensions.stream()
                .map(ext -> ext.startsWith(".") ? ext.substring(1).toLowerCase() : ext.toLowerCase())
                .anyMatch(ext -> ext.equals(fileExtension));
    }
}