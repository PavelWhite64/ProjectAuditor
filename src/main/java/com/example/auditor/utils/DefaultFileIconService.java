package com.example.auditor.utils;

import com.example.auditor.core.FileIconService;
import com.example.auditor.utils.FileExtensionUtils.ExtensionFormat;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация FileIconService по умолчанию
 */
public class DefaultFileIconService implements FileIconService {

    private final Map<String, String> icons;
    private final Map<String, String> htmlIcons;
    private final Map<String, String> languages;

    public DefaultFileIconService() {
        this.icons = initializeIcons();
        this.htmlIcons = initializeHtmlIcons();
        this.languages = initializeLanguages();
    }

    @Override
    public String getIcon(String fileName) {
        if (fileName == null) return icons.get("default_file");
        String lowerFileName = fileName.toLowerCase();
        String extension = FileExtensionUtils.getExtension(lowerFileName, ExtensionFormat.WITH_DOT);

        if (icons.containsKey(lowerFileName)) {
            return icons.get(lowerFileName);
        }
        return icons.getOrDefault(extension, icons.get("default_file"));
    }

    @Override
    public String getHtmlIcon(String fileName) {
        if (fileName == null) return htmlIcons.get("default_html");
        String lowerFileName = fileName.toLowerCase();
        String extension = FileExtensionUtils.getExtension(lowerFileName, ExtensionFormat.WITH_DOT);

        if (htmlIcons.containsKey(lowerFileName)) {
            return htmlIcons.get(lowerFileName);
        }
        return htmlIcons.getOrDefault(extension, htmlIcons.get("default_html"));
    }

    @Override
    public String getLanguage(String fileName) {
        if (fileName == null) return languages.get("default_lang");
        String lowerFileName = fileName.toLowerCase();
        String extension = FileExtensionUtils.getExtension(lowerFileName, ExtensionFormat.WITH_DOT);

        if (languages.containsKey(lowerFileName)) {
            return languages.get(lowerFileName);
        }
        return languages.getOrDefault(extension, languages.get("default_lang"));
    }

    private Map<String, String> initializeIcons() {
        Map<String, String> icons = new HashMap<>();
        // Иконки для терминала
        icons.put(".java", "[JAVA]");
        icons.put(".kt", "[JAVA]");
        icons.put(".scala", "[JAVA]");
        icons.put(".js", "[JS]");
        icons.put(".ts", "[JS]");
        icons.put(".jsx", "[JS]");
        icons.put(".tsx", "[JS]");
        icons.put(".vue", "[WEB]");
        icons.put(".svelte", "[WEB]");
        icons.put(".py", "[PYTHON]");
        icons.put(".go", "[GO]");
        icons.put(".rs", "[RUST]");
        icons.put(".cpp", "[C++]");
        icons.put(".c", "[C++]");
        icons.put(".h", "[C++]");
        icons.put(".hpp", "[C++]");
        icons.put(".cc", "[C++]");
        icons.put(".cs", "[C#]");
        icons.put(".vb", "[C#]");
        icons.put(".fs", "[C#]");
        icons.put(".php", "[PHP]");
        icons.put(".rb", "[RUBY]");
        icons.put(".swift", "[SWIFT]");
        icons.put(".yml", "[CONFIG]");
        icons.put(".yaml", "[CONFIG]");
        icons.put(".xml", "[DATA]");
        icons.put(".json", "[DATA]");
        icons.put(".toml", "[DATA]");
        icons.put(".ini", "[DATA]");
        icons.put(".md", "[DOC]");
        icons.put(".txt", "[DOC]");
        icons.put(".rst", "[DOC]");
        icons.put(".sql", "[SQL]");
        icons.put(".sh", "[SCRIPT]");
        icons.put(".ps1", "[SCRIPT]");
        icons.put(".bat", "[SCRIPT]");
        icons.put(".html", "[WEB]");
        icons.put(".htm", "[WEB]");
        icons.put(".css", "[WEB]");
        icons.put("dockerfile", "[DOCKER]");
        icons.put(".gitignore", "[GIT]");
        icons.put("default_file", "[FILE]");

        return icons;
    }

    private Map<String, String> initializeHtmlIcons() {
        Map<String, String> htmlIcons = new HashMap<>();
        // Иконки для HTML
        htmlIcons.put(".java", "☕");
        htmlIcons.put(".kt", "☕");
        htmlIcons.put(".scala", "☕");
        htmlIcons.put(".js", "🟨");
        htmlIcons.put(".ts", "🟨");
        htmlIcons.put(".jsx", "🟨");
        htmlIcons.put(".tsx", "🟨");
        htmlIcons.put(".vue", "🌐");
        htmlIcons.put(".svelte", "🌐");
        htmlIcons.put(".py", "🐍");
        htmlIcons.put(".go", "🐹");
        htmlIcons.put(".rs", "🦀");
        htmlIcons.put(".cpp", "🔵");
        htmlIcons.put(".c", "🔵");
        htmlIcons.put(".h", "🔵");
        htmlIcons.put(".hpp", "🔵");
        htmlIcons.put(".cc", "🔵");
        htmlIcons.put(".cs", "🔷");
        htmlIcons.put(".vb", "🔷");
        htmlIcons.put(".fs", "🔷");
        htmlIcons.put(".php", "🐘");
        htmlIcons.put(".rb", "💎");
        htmlIcons.put(".swift", "🔷");
        htmlIcons.put(".yml", "⚙️");
        htmlIcons.put(".yaml", "⚙️");
        htmlIcons.put(".xml", "📊");
        htmlIcons.put(".json", "📊");
        htmlIcons.put(".toml", "📊");
        htmlIcons.put(".ini", "📊");
        htmlIcons.put(".md", "📝");
        htmlIcons.put(".txt", "📝");
        htmlIcons.put(".rst", "📝");
        htmlIcons.put(".sql", "🗃️");
        htmlIcons.put(".sh", "💻");
        htmlIcons.put(".ps1", "💻");
        htmlIcons.put(".bat", "💻");
        htmlIcons.put(".html", "🌐");
        htmlIcons.put(".htm", "🌐");
        htmlIcons.put(".css", "🌐");
        htmlIcons.put("dockerfile", "🐳");
        htmlIcons.put(".gitignore", "🐙");
        htmlIcons.put("default_html", "📄");

        return htmlIcons;
    }

    private Map<String, String> initializeLanguages() {
        Map<String, String> languages = new HashMap<>();
        // Языки для подсветки синтаксиса
        languages.put(".java", "java");
        languages.put(".kt", "kotlin");
        languages.put(".scala", "scala");
        languages.put(".js", "javascript");
        languages.put(".ts", "typescript");
        languages.put(".jsx", "jsx");
        languages.put(".tsx", "tsx");
        languages.put(".vue", "vue");
        languages.put(".svelte", "svelte");
        languages.put(".py", "python");
        languages.put(".pyx", "cython");
        languages.put(".pxd", "cython");
        languages.put(".ipynb", "json");
        languages.put(".go", "go");
        languages.put(".rs", "rust");
        languages.put(".cpp", "cpp");
        languages.put(".c", "c");
        languages.put(".h", "cpp");
        languages.put(".hpp", "cpp");
        languages.put(".cc", "cpp");
        languages.put(".cxx", "cpp");
        languages.put(".cs", "csharp");
        languages.put(".vb", "vbnet");
        languages.put(".fs", "fsharp");
        languages.put(".fsx", "fsharp");
        languages.put(".php", "php");
        languages.put(".phtml", "php");
        languages.put(".php4", "php");
        languages.put(".php5", "php");
        languages.put(".php7", "php");
        languages.put(".rb", "ruby");
        languages.put(".erb", "erb");
        languages.put(".swift", "swift");
        languages.put(".yml", "yaml");
        languages.put(".yaml", "yaml");
        languages.put(".xml", "xml");
        languages.put(".json", "json");
        languages.put(".jsonc", "json");
        languages.put(".json5", "json");
        languages.put(".toml", "toml");
        languages.put(".ini", "ini");
        languages.put(".md", "markdown");
        languages.put(".txt", "text");
        languages.put(".rst", "rst");
        languages.put(".sql", "sql");
        languages.put(".sh", "bash");
        languages.put(".ps1", "powershell");
        languages.put(".bat", "batch");
        languages.put(".html", "html");
        languages.put(".htm", "html");
        languages.put(".css", "css");
        languages.put(".sass", "sass");
        languages.put(".less", "less");
        languages.put("dockerfile", "dockerfile");
        languages.put("makefile", "makefile");
        languages.put("cmakelists.txt", "cmake");
        languages.put("default_lang", "text");

        return languages;
    }
}