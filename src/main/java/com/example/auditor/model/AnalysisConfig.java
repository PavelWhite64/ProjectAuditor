package com.example.auditor.model;

import java.nio.file.Path;
import java.util.List;

/**
 * Класс для хранения конфигурации анализа проекта.
 */
public class AnalysisConfig {
    private final Path projectPath;
    private final OutputFormat outputFormat;
    private final String outputFileName;
    private final boolean generateJsonMetadata;
    private final boolean openResultsAfterwards;
    private final boolean useGitIgnore;
    private final long maxFileSizeKB; // Максимальный размер файла в KB
    private final List<String> excludedPatterns; // Паттерны исключения (из .gitignore и других)
    private final boolean lightMode; // Режим "только структура"

    // Конструктор с всеми параметрами
    public AnalysisConfig(Path projectPath, OutputFormat outputFormat, String outputFileName,
                          boolean generateJsonMetadata, boolean openResultsAfterwards,
                          boolean useGitIgnore, long maxFileSizeKB, List<String> excludedPatterns, boolean lightMode) {
        this.projectPath = projectPath;
        this.outputFormat = outputFormat;
        this.outputFileName = outputFileName;
        this.generateJsonMetadata = generateJsonMetadata;
        this.openResultsAfterwards = openResultsAfterwards;
        this.useGitIgnore = useGitIgnore;
        this.maxFileSizeKB = maxFileSizeKB;
        this.excludedPatterns = excludedPatterns != null ? excludedPatterns : List.of();
        this.lightMode = lightMode;
    }

    // Геттеры
    public Path getProjectPath() {
        return projectPath;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public boolean shouldGenerateJsonMetadata() {
        return generateJsonMetadata;
    }

    public boolean shouldOpenResultsAfterwards() {
        return openResultsAfterwards;
    }

    public boolean shouldUseGitIgnore() {
        return useGitIgnore;
    }

    public long getMaxFileSizeKB() {
        return maxFileSizeKB;
    }

    public List<String> getExcludedPatterns() {
        return excludedPatterns;
    }

    public boolean isLightMode() {
        return lightMode;
    }

    // Вспомогательный enum для формата вывода
    public enum OutputFormat {
        MARKDOWN,
        HTML,
        BOTH,
        STRUCTURE_ONLY // Соответствует "Только структура"
    }
}