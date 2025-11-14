package com.example.auditor;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectAnalyzer {

    public String getProjectType(String projectPath, List<FileScanner.FileInfo> files) {
        Map<String, List<String>> indicators = new HashMap<>();

        indicators.put("Node.js", Arrays.asList("package.json", "node_modules"));
        indicators.put("Python", Arrays.asList("requirements.txt", "pyproject.toml", "Pipfile", "__pycache__"));
        indicators.put("Java", Arrays.asList("pom.xml", "build.gradle", "*.java"));
        indicators.put("Go", Arrays.asList("go.mod", "go.sum", "*.go"));
        indicators.put("Rust", Arrays.asList("Cargo.toml", "Cargo.lock", "*.rs"));
        indicators.put(".NET", Arrays.asList("*.csproj", "*.sln", "*.vbproj"));
        indicators.put("Ruby", Arrays.asList("Gemfile", "Gemfile.lock", "*.rb"));
        indicators.put("PHP", Arrays.asList("composer.json", "composer.lock", "*.php"));
        indicators.put("Docker", Arrays.asList("Dockerfile", "docker-compose.yml"));
        indicators.put("Vue", Arrays.asList("vue.config.js", "*.vue"));
        indicators.put("Angular", Arrays.asList("angular.json", "*.component.ts"));

        Set<String> detectedTypes = new HashSet<>();
        Set<String> fileNames = files.stream().map(f -> f.name).collect(Collectors.toSet());

        for (Map.Entry<String, List<String>> entry : indicators.entrySet()) {
            String type = entry.getKey();
            List<String> markers = entry.getValue();

            for (String marker : markers) {
                if (marker.contains("*")) {
                    // Проверка шаблона
                    String ext = marker.substring(marker.indexOf("*"));
                    if (fileNames.stream().anyMatch(name -> name.endsWith(ext.replace("*", "")))) {
                        detectedTypes.add(type);
                        break;
                    }
                } else if (fileNames.contains(marker)) {
                    detectedTypes.add(type);
                    break;
                }
            }
        }

        if (detectedTypes.isEmpty()) {
            return "Не определен";
        }

        return String.join(", ", detectedTypes);
    }

    public static String getLanguageFromExtension(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex).toLowerCase();
        }

        switch (extension) {
            case ".java": case ".kt": case ".scala": return "java";
            case ".js": return "javascript";
            case ".ts": case ".tsx": return "typescript";
            case ".jsx": return "jsx";
            case ".vue": return "vue";
            case ".py": return "python";
            case ".go": return "go";
            case ".rs": return "rust";
            case ".cpp": case ".c": case ".h": case ".hpp": case ".cc": return "cpp";
            case ".cs": return "csharp";
            case ".php": return "php";
            case ".rb": return "ruby";
            case ".swift": return "swift";
            case ".yml": case ".yaml": return "yaml";
            case ".xml": return "xml";
            case ".json": case ".jsonc": case ".json5": return "json";
            case ".toml": return "toml";
            case ".md": return "markdown";
            case ".sql": return "sql";
            case ".sh": case ".bash": case ".zsh": return "bash";
            case ".ps1": return "powershell";
            case ".bat": case ".cmd": return "batch";
            case ".html": case ".htm": return "html";
            case ".css": return "css";
            case ".scss": return "scss";
            case ".sass": return "sass";
            case ".less": return "less";
            default:
                if ("Dockerfile".equals(fileName)) return "dockerfile";
                if ("Makefile".equals(fileName)) return "makefile";
                if ("CMakeLists.txt".equals(fileName)) return "cmake";
                return "text";
        }
    }

    // ЭТИ МЕТОДЫ БЫЛИ ПРОПУЩЕНЫ В ПРЕДЫДУЩЕЙ ВЕРСИИ
    public Map<String, Integer> getLanguageStats(List<FileScanner.FileInfo> files) {
        Map<String, Integer> stats = new HashMap<>();
        for (FileScanner.FileInfo file : files) {
            String lang = getLanguageFromExtension(file.name);
            stats.put(lang, stats.getOrDefault(lang, 0) + 1);
        }
        return stats;
    }

    public Map<String, Integer> getSizeStats(List<FileScanner.FileInfo> files) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("0-1KB", 0);
        stats.put("1-10KB", 0);
        stats.put("10-100KB", 0);
        stats.put("100-500KB", 0);
        stats.put("500KB+", 0);

        for (FileScanner.FileInfo file : files) {
            double sizeKB = file.length / 1024.0;
            if (sizeKB < 1) {
                stats.put("0-1KB", stats.get("0-1KB") + 1);
            } else if (sizeKB < 10) {
                stats.put("1-10KB", stats.get("1-10KB") + 1);
            } else if (sizeKB < 100) {
                stats.put("10-100KB", stats.get("10-100KB") + 1);
            } else if (sizeKB < 500) {
                stats.put("100-500KB", stats.get("100-500KB") + 1);
            } else {
                stats.put("500KB+", stats.get("500KB+") + 1);
            }
        }

        return stats;
    }
}