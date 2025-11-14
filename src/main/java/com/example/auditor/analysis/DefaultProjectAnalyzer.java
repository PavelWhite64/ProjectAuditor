// src/main/java/com/example/auditor/analysis/DefaultProjectAnalyzer.java
package com.example.auditor.analysis;

import com.example.auditor.core.ProjectAnalyzer;
import com.example.auditor.core.ProjectScanner;
import com.example.auditor.core.FileFilter;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.model.FileInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация ProjectAnalyzer, координирующая сканирование и фильтрацию.
 */
public class DefaultProjectAnalyzer implements ProjectAnalyzer {

    private final ProjectScanner scanner;
    private final FileFilter fileFilter;

    public DefaultProjectAnalyzer(ProjectScanner scanner, FileFilter fileFilter) {
        this.scanner = scanner;
        this.fileFilter = fileFilter;
    }

    @Override
    public AnalysisResult analyze(AnalysisConfig config) {
        Path projectPath = config.getProjectPath();

        // 1. Сканируем проект
        System.out.println("Сканирование проекта: " + projectPath);
        List<FileInfo> allFiles = scanner.scan(projectPath);

        // 2. Фильтруем файлы
        System.out.println("Фильтрация файлов...");
        List<FileInfo> filteredFiles = fileFilter.filter(allFiles, projectPath, config);

        // 3. Определяем тип проекта и собираем метаданные
        String projectName = projectPath.getFileName().toString();
        String projectType = determineProjectType(filteredFiles, projectPath); // Передаем projectPath
        long totalSizeKB = filteredFiles.stream().mapToLong(FileInfo::getLength).sum() / 1024;
        int totalFiles = filteredFiles.size();

        System.out.println("Анализ завершен. Найдено " + totalFiles + " файлов.");

        // 4. Возвращаем результат
        return new AnalysisResult(filteredFiles, projectName, projectType, totalSizeKB, totalFiles);
    }

    // --- Вспомогательный метод для определения типа проекта ---
    private String determineProjectType(List<FileInfo> files, Path projectPath) {
        // Проверяем наличие характерных файлов в корне проекта
        Set<String> rootFileNames = files.stream()
                .map(file -> file.getRelativePath()) // Получаем относительный путь
                .map(path -> {
                    // Получаем имя файла из пути
                    int lastSlashIndex = path.lastIndexOf('/');
                    if (lastSlashIndex == -1) { // Файл в корне
                        return path.toLowerCase();
                    } else {
                        return path.substring(lastSlashIndex + 1).toLowerCase();
                    }
                })
                .collect(Collectors.toSet());

        // Проверяем корневые файлы
        if (rootFileNames.contains("pom.xml")) return "Java (Maven)";
        if (rootFileNames.contains("build.gradle") || rootFileNames.contains("build.gradle.kts")) return "Java/Gradle";
        if (rootFileNames.contains("go.mod")) return "Go";
        if (rootFileNames.contains("Cargo.toml")) return "Rust";
        if (rootFileNames.contains("package.json")) return "JavaScript/Node.js";
        if (rootFileNames.contains("requirements.txt") || rootFileNames.contains("setup.py")) return "Python";
        if (rootFileNames.contains("Gemfile")) return "Ruby";
        if (rootFileNames.contains("composer.json")) return "PHP";
        if (rootFileNames.contains("Dockerfile")) return "Docker";
        // ... добавьте другие характерные файлы ...

        // Если характерных файлов нет, анализируем расширения
        Map<String, Long> extensionCount = files.stream()
                .collect(Collectors.groupingBy(FileInfo::getExtension, Collectors.counting()));

        // Примеры определения типа на основе файлов
        if (extensionCount.containsKey("java") && (extensionCount.containsKey("xml") || extensionCount.containsKey("gradle") || extensionCount.containsKey("pom"))) {
            return "Java";
        } else if (extensionCount.containsKey("py") && extensionCount.containsKey("py")) {
            return "Python";
        } else if (extensionCount.containsKey("js") && extensionCount.containsKey("json")) {
            return "JavaScript/Node.js";
        } else if (extensionCount.containsKey("go") && extensionCount.containsKey("go")) {
            return "Go";
        } else if (extensionCount.containsKey("cs") && extensionCount.containsKey("csproj")) {
            return ".NET";
        } else if (extensionCount.containsKey("rs") && extensionCount.containsKey("toml")) {
            return "Rust";
        }
        // ... другие типы на основе расширений ...
        return "Generic";
    }
}