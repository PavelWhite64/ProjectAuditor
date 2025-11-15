package com.example.auditor.model;

import java.nio.file.Path;
import java.util.List;

/**
 * Класс для хранения конфигурации анализа проекта.
 * Добавлены ограничения для обработки больших файлов.
 */
public class AnalysisConfig {
    private final Path projectPath;
    private final OutputFormat outputFormat;
    private final String outputFileName;
    private final boolean generateJsonMetadata;
    private final boolean openResultsAfterwards;
    private final boolean useGitIgnore;
    private final long maxFileSizeKB; // Максимальный размер файла в KB для фильтрации
    private final List<String> excludedPatterns;
    private final boolean lightMode;

    // Новые поля для ограничения обработки больших файлов
    private final long maxContentSizeBytes; // Максимальный размер содержимого для чтения (в байтах)
    private final int maxLinesPerFile;      // Максимальное количество строк для чтения

    // Конструктор с всеми параметрами
    public AnalysisConfig(Path projectPath, OutputFormat outputFormat, String outputFileName,
                          boolean generateJsonMetadata, boolean openResultsAfterwards,
                          boolean useGitIgnore, long maxFileSizeKB, List<String> excludedPatterns,
                          boolean lightMode) {
        this(projectPath, outputFormat, outputFileName, generateJsonMetadata, openResultsAfterwards,
                useGitIgnore, maxFileSizeKB, excludedPatterns, lightMode,
                50 * 1024 * 1024, // 50MB по умолчанию для содержимого
                5000); // 5000 строк по умолчанию
    }

    // Новый конструктор с ограничениями содержимого
    public AnalysisConfig(Path projectPath, OutputFormat outputFormat, String outputFileName,
                          boolean generateJsonMetadata, boolean openResultsAfterwards,
                          boolean useGitIgnore, long maxFileSizeKB, List<String> excludedPatterns,
                          boolean lightMode, long maxContentSizeBytes, int maxLinesPerFile) {
        this.projectPath = projectPath;
        this.outputFormat = outputFormat;
        this.outputFileName = outputFileName;
        this.generateJsonMetadata = generateJsonMetadata;
        this.openResultsAfterwards = openResultsAfterwards;
        this.useGitIgnore = useGitIgnore;
        this.maxFileSizeKB = maxFileSizeKB;
        this.excludedPatterns = excludedPatterns != null ? excludedPatterns : List.of();
        this.lightMode = lightMode;
        this.maxContentSizeBytes = maxContentSizeBytes;
        this.maxLinesPerFile = maxLinesPerFile;
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

    // Новые геттеры для ограничений содержимого
    public long getMaxContentSizeBytes() {
        return maxContentSizeBytes;
    }

    public int getMaxLinesPerFile() {
        return maxLinesPerFile;
    }

    // Вспомогательный enum для формата вывода
    public enum OutputFormat {
        MARKDOWN,
        HTML,
        BOTH,
        STRUCTURE_ONLY
    }
}