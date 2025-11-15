package com.example.auditor.reporting;

import com.example.auditor.core.FileIconService;
import com.example.auditor.core.ReportStrategy;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.model.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * Стратегия генерации HTML отчетов
 */
public class HtmlReportStrategy implements ReportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportStrategy.class);

    private final FileIconService fileIconService;
    private final HtmlReportGenerator htmlGenerator;

    public HtmlReportStrategy(FileIconService fileIconService) {
        this.fileIconService = fileIconService;
        this.htmlGenerator = new HtmlReportGenerator(fileIconService);
    }

    @Override
    public boolean supports(AnalysisConfig.OutputFormat format) {
        return format == AnalysisConfig.OutputFormat.HTML ||
                format == AnalysisConfig.OutputFormat.BOTH;
    }

    @Override
    public void generateReport(AnalysisResult result, AnalysisConfig config, Path outputDir, String outputFileName) {
        String htmlFile = outputDir.resolve(outputFileName + getFileExtension()).toString();
        List<FileInfo> files = result.getFileInfoList();
        String projectName = result.getProjectName();
        String projectType = result.getProjectType();
        boolean lightMode = config.isLightMode();
        Path projectPath = config.getProjectPath();

        // Получаем ограничения из конфигурации
        long maxContentSizeBytes = config.getMaxContentSizeBytes();
        int maxLinesPerFile = config.getMaxLinesPerFile();

        htmlGenerator.generate(files, projectName, projectType, lightMode, projectPath, htmlFile,
                maxContentSizeBytes, maxLinesPerFile);
    }

    @Override
    public String getFileExtension() {
        return ".html";
    }

    @Override
    public String getFormatDescription() {
        return "HTML (.html) - для веб-просмотра";
    }
}