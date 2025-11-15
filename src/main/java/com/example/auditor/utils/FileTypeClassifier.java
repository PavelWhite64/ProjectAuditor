package com.example.auditor.utils;

/**
 * Утилитарный класс для классификации типа файла на основе его имени или расширения.
 */
public class FileTypeClassifier {

    /**
     * Классифицирует файл на основе его имени.
     *
     * @param fileName Имя файла (например, "Main.java", "config.json", "Dockerfile").
     * @return Тип файла в виде строки (например, "JAVA", "JSON", "SCRIPT", "DOC").
     */
    public static String classify(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "FILE"; // Значение по умолчанию
        }

        String lowerFileName = fileName.toLowerCase();
        String extension = getFileExtension(lowerFileName);

        // Классификация по расширению
        if (extension != null) {
            switch (extension) {
                // Исходный код (приоритетный тип)
                case "java":
                case "kt":
                case "scala":
                case "groovy":
                case "js":
                case "ts":
                case "jsx":
                case "tsx":
                case "vue":
                case "svelte":
                case "go":
                case "rs":
                case "cpp":
                case "c":
                case "h":
                case "hpp":
                case "cc":
                case "cs":
                case "vb":
                case "fs":
                case "php":
                case "rb":
                case "pl":
                case "pm":
                case "sql":
                case "py": // .py обычно исходный код
                    return "CODE";

                // Скрипты (ниже приоритет, чем CODE, но выше CONFIG/DATA/DOC)
                // .py может быть и скриптом, но в "CODE" выше он уже учтён
                case "sh":
                case "bat":
                case "cmd":
                case "ps1":
                    return "SCRIPT";

                // Конфигурационные файлы (ниже SCRIPT)
                case "json":
                case "yml":
                case "yaml":
                case "xml":
                case "ini":
                case "cfg":
                case "toml":
                case "properties": // .properties как конфиг
                    return "CONFIG";

                // Данные (ниже CONFIG)
                case "csv":
                case "db":
                case "sqlite":
                    return "DATA";

                // Документация (ниже DATA)
                case "md":
                case "txt":
                case "rst":
                    return "DOC";

                // Бинарные файлы (ниже DOC)
                case "jar":
                case "war":
                case "ear":
                case "class":
                case "exe":
                case "dll":
                case "so":
                case "dylib":
                case "png":
                case "jpg":
                case "jpeg":
                case "gif":
                case "bmp":
                case "ico":
                case "svg":
                case "zip":
                case "rar":
                case "7z":
                case "tar":
                case "gz":
                case "mp3":
                case "mp4":
                case "avi":
                case "mov":
                case "wav":
                case "flac":
                case "ttf":
                case "otf":
                case "woff":
                case "woff2":
                case "pdf":
                case "doc":
                case "docx": // Также бинарные
                    return "BINARY";

                // Сборка/зависимости
                case "lock": // package-lock.json, yarn.lock, Gemfile.lock, etc.
                    return "BUILD_ARTIFACT";

                default:
                    // Проверим, не является ли файл специфичным по имени
                    if (isSpecificBuildOrConfigFile(lowerFileName)) {
                        return "CONFIG"; // Или BUILD_ARTIFACT, в зависимости от типа
                    }
                    break;
            }
        } else {
            // Нет расширения, проверим по имени
            if (isSpecificBuildOrConfigFile(lowerFileName)) {
                return "CONFIG"; // Или BUILD_ARTIFACT
            }
        }

        // Значение по умолчанию, если не подошло ни одно правило
        return "FILE";
    }

    /**
     * Извлекает расширение файла (без точки).
     *
     * @param fileName Имя файла.
     * @return Расширение или null, если нет.
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
    }

    /**
     * Проверяет, является ли файл специфичным по имени (например, Dockerfile, pom.xml).
     *
     * @param fileName Имя файла.
     * @return true, если файл специфичный.
     */
    private static boolean isSpecificBuildOrConfigFile(String fileName) {
        return "dockerfile".equals(fileName) || "docker-compose.yml".equals(fileName) || "docker-compose.yaml".equals(fileName) ||
                "pom.xml".equals(fileName) || "build.gradle".equals(fileName) || "build.gradle.kts".equals(fileName) ||
                "settings.gradle".equals(fileName) || "settings.gradle.kts".equals(fileName) ||
                "package.json".equals(fileName) || "requirements.txt".equals(fileName) ||
                ".gitignore".equals(fileName) || ".env".equals(fileName) || ".env.local".equals(fileName) ||
                "readme.md".equals(fileName) || "changelog.md".equals(fileName) || "contributing.md".equals(fileName) ||
                "license".equals(fileName) || "license.txt".equals(fileName) || "makefile".equals(fileName) ||
                "cmakelists.txt".equals(fileName) || "jenkinsfile".equals(fileName) || ".travis.yml".equals(fileName) ||
                ".github".equals(fileName) || ".gitlab".equals(fileName) || // Папки как файлы?
                "go.mod".equals(fileName) || "go.sum".equals(fileName) || "cargo.toml".equals(fileName) ||
                "cargo.lock".equals(fileName) || "composer.json".equals(fileName) || "composer.lock".equals(fileName);
    }
}