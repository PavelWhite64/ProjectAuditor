package com.example.auditor.reporting;

import com.example.auditor.core.FileIconService;
import com.example.auditor.core.ReportStrategy;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Стратегия генерации JSON метаданных
 */
public class JsonReportStrategy implements ReportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonReportStrategy.class);

    private final FileIconService fileIconService;
    private final JsonMetadataGenerator jsonGenerator;

    public JsonReportStrategy(FileIconService fileIconService) {
        this.fileIconService = fileIconService;
        this.jsonGenerator = new JsonMetadataGenerator(fileIconService);
    }

    @Override
    public boolean supports(AnalysisConfig.OutputFormat format) {
        // JSON генерируется отдельно от основных форматов, всегда доступен если включена опция
        return true; // Всегда доступен, если пользователь выбрал генерацию JSON
    }

    @Override
    public void generateReport(AnalysisResult result, AnalysisConfig config, Path outputDir, String outputFileName) {
        if (config.shouldGenerateJsonMetadata()) {
            String jsonFile = outputDir.resolve(outputFileName + getFileExtension()).toString();
            jsonGenerator.generate(result, jsonFile);
        }
    }

    @Override
    public String getFileExtension() {
        return ".json";
    }

    @Override
    public String getFormatDescription() {
        return "JSON (.json) - метаданные для автоматической обработки";
    }
}