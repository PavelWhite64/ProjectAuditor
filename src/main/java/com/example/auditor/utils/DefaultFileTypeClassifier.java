package com.example.auditor.utils;

import com.example.auditor.core.FileTypeClassifier;

/**
 * Реализация FileTypeClassifier по умолчанию
 */
public class DefaultFileTypeClassifier implements FileTypeClassifier {

    @Override
    public String classify(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "FILE";
        }

        String lowerFileName = fileName.toLowerCase();
        String extension = FileExtensionUtils.getExtension(lowerFileName,
                FileExtensionUtils.ExtensionFormat.WITHOUT_DOT);

        // Классификация по расширению
        if (extension != null) {
            switch (extension) {
                // Исходный код (приоритетный тип)
                case "java": case "kt": case "scala": case "groovy":
                case "js": case "ts": case "jsx": case "tsx": case "vue": case "svelte":
                case "go": case "rs": case "cpp": case "c": case "h": case "hpp": case "cc":
                case "cs": case "vb": case "fs": case "php": case "rb": case "pl": case "pm":
                case "sql": case "py":
                    return "CODE";

                // Скрипты
                case "sh": case "bat": case "cmd": case "ps1":
                    return "SCRIPT";

                // Конфигурационные файлы
                case "json": case "yml": case "yaml": case "xml":
                case "ini": case "cfg": case "toml": case "properties":
                    return "CONFIG";

                // Данные
                case "csv": case "db": case "sqlite":
                    return "DATA";

                // Документация
                case "md": case "txt": case "rst":
                    return "DOC";

                // Бинарные файлы
                case "jar": case "war": case "ear": case "class":
                case "exe": case "dll": case "so": case "dylib":
                case "png": case "jpg": case "jpeg": case "gif": case "bmp": case "ico": case "svg":
                case "zip": case "rar": case "7z": case "tar": case "gz":
                case "mp3": case "mp4": case "avi": case "mov": case "wav": case "flac":
                case "ttf": case "otf": case "woff": case "woff2":
                case "pdf": case "doc": case "docx":
                    return "BINARY";

                // Сборка/зависимости
                case "lock":
                    return "BUILD_ARTIFACT";

                default:
                    // Проверим, не является ли файл специфичным по имени
                    if (isSpecificBuildOrConfigFile(lowerFileName)) {
                        return "CONFIG";
                    }
                    break;
            }
        } else {
            // Нет расширения, проверим по имени
            if (isSpecificBuildOrConfigFile(lowerFileName)) {
                return "CONFIG";
            }
        }

        return "FILE";
    }

    /**
     * Проверяет, является ли файл специфичным по имени
     */
    private boolean isSpecificBuildOrConfigFile(String fileName) {
        return "dockerfile".equals(fileName) ||
                "docker-compose.yml".equals(fileName) ||
                "docker-compose.yaml".equals(fileName) ||
                "pom.xml".equals(fileName) ||
                "build.gradle".equals(fileName) ||
                "build.gradle.kts".equals(fileName) ||
                "settings.gradle".equals(fileName) ||
                "settings.gradle.kts".equals(fileName) ||
                "package.json".equals(fileName) ||
                "requirements.txt".equals(fileName) ||
                ".gitignore".equals(fileName) ||
                ".env".equals(fileName) ||
                ".env.local".equals(fileName) ||
                "readme.md".equals(fileName) ||
                "changelog.md".equals(fileName) ||
                "contributing.md".equals(fileName) ||
                "license".equals(fileName) ||
                "license.txt".equals(fileName) ||
                "makefile".equals(fileName) ||
                "cmakelists.txt".equals(fileName) ||
                "jenkinsfile".equals(fileName) ||
                ".travis.yml".equals(fileName) ||
                "go.mod".equals(fileName) ||
                "go.sum".equals(fileName) ||
                "cargo.toml".equals(fileName) ||
                "cargo.lock".equals(fileName) ||
                "composer.json".equals(fileName) ||
                "composer.lock".equals(fileName);
    }
}