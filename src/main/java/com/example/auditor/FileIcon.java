package com.example.auditor;

public class FileIcon {

    public static String getIcon(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex).toLowerCase();
        }

        switch (extension) {
            case ".java": case ".kt": case ".scala": return "[JAVA]";
            case ".js": case ".ts": case ".jsx": case ".tsx": return "[JS]";
            case ".vue": case ".svelte": return "[WEB]";
            case ".py": return "[PYTHON]";
            case ".go": return "[GO]";
            case ".rs": return "[RUST]";
            case ".cpp": case ".c": case ".h": case ".hpp": case ".cc": return "[C++]";
            case ".cs": case ".vb": case ".fs": return "[C#]";
            case ".php": return "[PHP]";
            case ".rb": return "[RUBY]";
            case ".swift": return "[SWIFT]";
            case ".yml": case ".yaml": return "[CONFIG]";
            case ".xml": case ".json": case ".toml": case ".ini": return "[DATA]";
            case ".md": case ".txt": case ".rst": return "[DOC]";
            case ".sql": return "[SQL]";
            case ".sh": case ".ps1": case ".bat": return "[SCRIPT]";
            case ".html": case ".htm": case ".css": return "[WEB]";
            default:
                if ("Dockerfile".equals(fileName)) return "[DOCKER]";
                if (".gitignore".equals(fileName)) return "[GIT]";
                return "[FILE]";
        }
    }

    public static String getHtmlIcon(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex).toLowerCase();
        }

        switch (extension) {
            case ".java": case ".kt": case ".scala": return "☕";
            case ".js": case ".ts": case ".jsx": case ".tsx": return "🟨";
            case ".vue": case ".svelte": return "🌐";
            case ".py": return "🐍";
            case ".go": return "🐹";
            case ".rs": return "🦀";
            case ".cpp": case ".c": case ".h": case ".hpp": case ".cc": return "🔵";
            case ".cs": case ".vb": case ".fs": return "🔷";
            case ".php": return "🐘";
            case ".rb": return "💎";
            case ".swift": return "🔷";
            case ".yml": case ".yaml": return "⚙️";
            case ".xml": case ".json": case ".toml": case ".ini": return "📊";
            case ".md": case ".txt": case ".rst": return "📝";
            case ".sql": return "🗃️";
            case ".sh": case ".ps1": case ".bat": return "💻";
            case ".html": case ".htm": case ".css": return "🌐";
            default:
                if ("Dockerfile".equals(fileName)) return "🐳";
                if (".gitignore".equals(fileName)) return "🐙";
                return "📄";
        }
    }
}