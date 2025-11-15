package com.example.auditor.reporting;

import com.example.auditor.core.FileIconService;
import com.example.auditor.core.FileSystem;
import com.example.auditor.core.ReportStrategy;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.model.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * Стратегия генерации Markdown отчетов
 */
public class MarkdownReportStrategy implements ReportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownReportStrategy.class);

    private final FileIconService fileIconService;
    private final FileSystem fileSystem;
    private final MarkdownReportGenerator markdownGenerator;

    public MarkdownReportStrategy(FileIconService fileIconService, FileSystem fileSystem) {
        this.fileIconService = fileIconService;
        this.fileSystem = fileSystem;
        this.markdownGenerator = new MarkdownReportGenerator(fileIconService, fileSystem);
    }

    @Override
    public boolean supports(AnalysisConfig.OutputFormat format) {
        return format == AnalysisConfig.OutputFormat.MARKDOWN ||
                format == AnalysisConfig.OutputFormat.BOTH ||
                format == AnalysisConfig.OutputFormat.STRUCTURE_ONLY;
    }

    @Override
    public void generateReport(AnalysisResult result, AnalysisConfig config, Path outputDir, String outputFileName) {
        String markdownFile = outputDir.resolve(outputFileName + getFileExtension()).toString();
        List<FileInfo> files = result.getFileInfoList();
        String projectName = result.getProjectName();
        String projectType = result.getProjectType();
        boolean lightMode = config.isLightMode();
        Path projectPath = config.getProjectPath();

        // Получаем ограничения из конфигурации
        long maxContentSizeBytes = config.getMaxContentSizeBytes();
        int maxLinesPerFile = config.getMaxLinesPerFile();

        markdownGenerator.generate(files, projectName, projectType, lightMode, projectPath, markdownFile,
                maxContentSizeBytes, maxLinesPerFile);
    }

    @Override
    public String getFileExtension() {
        return ".md";
    }

    @Override
    public String getFormatDescription() {
        return "Markdown (.md) - для анализа LLM";
    }
}