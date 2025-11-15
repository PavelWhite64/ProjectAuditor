package com.example.auditor.reporting;

import com.example.auditor.core.FileIconService;
import com.example.auditor.core.ReportStrategy;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.model.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Стратегия генерации Markdown отчетов
 */
public class MarkdownReportStrategy implements ReportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownReportStrategy.class);

    private final FileIconService fileIconService;
    private final MarkdownReportGenerator markdownGenerator;

    public MarkdownReportStrategy(FileIconService fileIconService) {
        this.fileIconService = fileIconService;
        this.markdownGenerator = new MarkdownReportGenerator(fileIconService);
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

        markdownGenerator.generate(files, projectName, projectType, lightMode, projectPath, markdownFile);
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