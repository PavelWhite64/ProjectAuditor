package com.example.auditor.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Утилитарный класс для получения иконок и языков программирования
 * на основе расширения или имени файла.
 */
public class FileIcon {

    // Иконки для терминала (используются в Markdown)
    private static final Map<String, String> ICONS = new HashMap<>();
    // Иконки для HTML
    private static final Map<String, String> HTML_ICONS = new HashMap<>();
    // Языки для подсветки синтаксиса
    private static final Map<String, String> LANGUAGES = new HashMap<>();

    static {
        // --- Иконки для терминала ---
        ICONS.put(".java", "[JAVA]");
        ICONS.put(".kt", "[JAVA]");
        ICONS.put(".scala", "[JAVA]");
        ICONS.put(".js", "[JS]");
        ICONS.put(".ts", "[JS]");
        ICONS.put(".jsx", "[JS]");
        ICONS.put(".tsx", "[JS]");
        ICONS.put(".vue", "[WEB]");
        ICONS.put(".svelte", "[WEB]");
        ICONS.put(".py", "[PYTHON]");
        ICONS.put(".go", "[GO]");
        ICONS.put(".rs", "[RUST]");
        ICONS.put(".cpp", "[C++]");
        ICONS.put(".c", "[C++]");
        ICONS.put(".h", "[C++]");
        ICONS.put(".hpp", "[C++]");
        ICONS.put(".cc", "[C++]");
        ICONS.put(".cs", "[C#]");
        ICONS.put(".vb", "[C#]");
        ICONS.put(".fs", "[C#]");
        ICONS.put(".php", "[PHP]");
        ICONS.put(".rb", "[RUBY]");
        ICONS.put(".swift", "[SWIFT]");
        ICONS.put(".yml", "[CONFIG]");
        ICONS.put(".yaml", "[CONFIG]");
        ICONS.put(".xml", "[DATA]");
        ICONS.put(".json", "[DATA]");
        ICONS.put(".toml", "[DATA]");
        ICONS.put(".ini", "[DATA]");
        ICONS.put(".md", "[DOC]");
        ICONS.put(".txt", "[DOC]");
        ICONS.put(".rst", "[DOC]");
        ICONS.put(".sql", "[SQL]");
        ICONS.put(".sh", "[SCRIPT]");
        ICONS.put(".ps1", "[SCRIPT]");
        ICONS.put(".bat", "[SCRIPT]");
        ICONS.put(".html", "[WEB]");
        ICONS.put(".htm", "[WEB]");
        ICONS.put(".css", "[WEB]");
        ICONS.put("dockerfile", "[DOCKER]"); // Точное совпадение имени файла
        ICONS.put(".gitignore", "[GIT]"); // Точное совпадение имени файла
        ICONS.put("default_file", "[FILE]"); // Значение по умолчанию для файлов

        // --- Иконки для HTML ---
        HTML_ICONS.put(".java", "☕");
        HTML_ICONS.put(".kt", "☕");
        HTML_ICONS.put(".scala", "☕");
        HTML_ICONS.put(".js", "🟨");
        HTML_ICONS.put(".ts", "🟨");
        HTML_ICONS.put(".jsx", "🟨");
        HTML_ICONS.put(".tsx", "🟨");
        HTML_ICONS.put(".vue", "🌐");
        HTML_ICONS.put(".svelte", "🌐");
        HTML_ICONS.put(".py", "🐍");
        HTML_ICONS.put(".go", "🐹");
        HTML_ICONS.put(".rs", "🦀");
        HTML_ICONS.put(".cpp", "🔵");
        HTML_ICONS.put(".c", "🔵");
        HTML_ICONS.put(".h", "🔵");
        HTML_ICONS.put(".hpp", "🔵");
        HTML_ICONS.put(".cc", "🔵");
        HTML_ICONS.put(".cs", "🔷");
        HTML_ICONS.put(".vb", "🔷");
        HTML_ICONS.put(".fs", "🔷");
        HTML_ICONS.put(".php", "🐘");
        HTML_ICONS.put(".rb", "💎");
        HTML_ICONS.put(".swift", "🔷");
        HTML_ICONS.put(".yml", "⚙️");
        HTML_ICONS.put(".yaml", "⚙️");
        HTML_ICONS.put(".xml", "📊");
        HTML_ICONS.put(".json", "📊");
        HTML_ICONS.put(".toml", "📊");
        HTML_ICONS.put(".ini", "📊");
        HTML_ICONS.put(".md", "📝");
        HTML_ICONS.put(".txt", "📝");
        HTML_ICONS.put(".rst", "📝");
        HTML_ICONS.put(".sql", "🗃️");
        HTML_ICONS.put(".sh", "💻");
        HTML_ICONS.put(".ps1", "💻");
        HTML_ICONS.put(".bat", "💻");
        HTML_ICONS.put(".html", "🌐");
        HTML_ICONS.put(".htm", "🌐");
        HTML_ICONS.put(".css", "🌐");
        HTML_ICONS.put("dockerfile", "🐳");
        HTML_ICONS.put(".gitignore", "🐙");
        HTML_ICONS.put("default_html", "📄"); // Значение по умолчанию для HTML

        // --- Языки для подсветки синтаксиса ---
        LANGUAGES.put(".java", "java");
        LANGUAGES.put(".kt", "kotlin");
        LANGUAGES.put(".scala", "scala");
        LANGUAGES.put(".js", "javascript");
        LANGUAGES.put(".ts", "typescript");
        LANGUAGES.put(".jsx", "jsx");
        LANGUAGES.put(".tsx", "tsx");
        LANGUAGES.put(".vue", "vue");
        LANGUAGES.put(".svelte", "svelte");
        LANGUAGES.put(".py", "python");
        LANGUAGES.put(".pyx", "cython");
        LANGUAGES.put(".pxd", "cython");
        LANGUAGES.put(".ipynb", "json");
        LANGUAGES.put(".go", "go");
        LANGUAGES.put(".rs", "rust");
        LANGUAGES.put(".cpp", "cpp");
        LANGUAGES.put(".c", "c");
        LANGUAGES.put(".h", "cpp"); // Заголовочные файлы C часто подсвечиваются как C++
        LANGUAGES.put(".hpp", "cpp");
        LANGUAGES.put(".cc", "cpp");
        LANGUAGES.put(".cxx", "cpp");
        LANGUAGES.put(".cs", "csharp");
        LANGUAGES.put(".vb", "vbnet");
        LANGUAGES.put(".fs", "fsharp");
        LANGUAGES.put(".fsx", "fsharp");
        LANGUAGES.put(".php", "php");
        LANGUAGES.put(".phtml", "php");
        LANGUAGES.put(".php4", "php");
        LANGUAGES.put(".php5", "php");
        LANGUAGES.put(".php7", "php");
        LANGUAGES.put(".rb", "ruby");
        LANGUAGES.put(".erb", "erb");
        LANGUAGES.put(".swift", "swift");
        LANGUAGES.put(".yml", "yaml");
        LANGUAGES.put(".yaml", "yaml");
        LANGUAGES.put(".xml", "xml");
        LANGUAGES.put(".json", "json");
        LANGUAGES.put(".jsonc", "json");
        LANGUAGES.put(".json5", "json");
        LANGUAGES.put(".toml", "toml");
        LANGUAGES.put(".ini", "ini");
        LANGUAGES.put(".md", "markdown");
        LANGUAGES.put(".txt", "text");
        LANGUAGES.put(".rst", "rst");
        LANGUAGES.put(".sql", "sql");
        LANGUAGES.put(".sh", "bash");
        LANGUAGES.put(".ps1", "powershell");
        LANGUAGES.put(".bat", "batch");
        LANGUAGES.put(".html", "html");
        LANGUAGES.put(".htm", "html");
        LANGUAGES.put(".css", "css");
        LANGUAGES.put(".sass", "sass");
        LANGUAGES.put(".less", "less");
        // Имена файлов без расширения
        LANGUAGES.put("dockerfile", "dockerfile");
        LANGUAGES.put("makefile", "makefile");
        LANGUAGES.put("cmakelists.txt", "cmake");
        LANGUAGES.put("default_lang", "text"); // Язык по умолчанию
    }

    /**
     * Возвращает иконку для терминала на основе имени файла.
     *
     * @param fileName Имя файла (например, "App.java", "Dockerfile").
     * @return Иконка в виде строки.
     */
    public static String getIcon(String fileName) {
        if (fileName == null) return ICONS.get("default_file");
        String lowerFileName = fileName.toLowerCase();
        String extension = extractExtension(lowerFileName);
        // Сначала проверяем точное совпадение имени файла (например, Dockerfile)
        if (ICONS.containsKey(lowerFileName)) {
            return ICONS.get(lowerFileName);
        }
        // Затем проверяем расширение
        return ICONS.getOrDefault(extension, ICONS.get("default_file"));
    }

    /**
     * Возвращает иконку для HTML на основе имени файла.
     *
     * @param fileName Имя файла.
     * @return Иконка в виде строки.
     */
    public static String getHtmlIcon(String fileName) {
        if (fileName == null) return HTML_ICONS.get("default_html");
        String lowerFileName = fileName.toLowerCase();
        String extension = extractExtension(lowerFileName);
        if (HTML_ICONS.containsKey(lowerFileName)) {
            return HTML_ICONS.get(lowerFileName);
        }
        return HTML_ICONS.getOrDefault(extension, HTML_ICONS.get("default_html"));
    }

    /**
     * Возвращает язык программирования для подсветки синтаксиса на основе имени файла.
     *
     * @param fileName Имя файла.
     * @return Язык в виде строки.
     */
    public static String getLanguage(String fileName) {
        if (fileName == null) return LANGUAGES.get("default_lang");
        String lowerFileName = fileName.toLowerCase();
        String extension = extractExtension(lowerFileName);
        if (LANGUAGES.containsKey(lowerFileName)) {
            return LANGUAGES.get(lowerFileName);
        }
        return LANGUAGES.getOrDefault(extension, LANGUAGES.get("default_lang"));
    }

    // Вспомогательный метод для извлечения расширения
    private static String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex); // Включаем точку
        }
        return null; // или возвращаем пустую строку ""
    }
}