package com.example.auditor.reporting;

import com.example.auditor.core.FileIconService;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.model.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonMetadataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMetadataGenerator.class);

    private final FileIconService fileIconService;

    public JsonMetadataGenerator(FileIconService fileIconService) {
        this.fileIconService = fileIconService;
    }

    public void generate(AnalysisResult result, String outputFile) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            writer.write("{\n");
            writer.write("  \"metadata\": {\n");
            writer.write("    \"projectName\": \"" + ReportUtils.escapeJson(result.getProjectName()) + "\",\n");
            writer.write("    \"totalFiles\": " + result.getTotalFiles() + ",\n");
            writer.write("    \"totalSizeKB\": " + result.getTotalSizeKB() + ",\n");
            writer.write("    \"projectType\": \"" + ReportUtils.escapeJson(result.getProjectType()) + "\",\n");
            writer.write("    \"generatedAt\": \"" + ReportUtils.getCurrentDate() + "\"\n");
            writer.write("  },\n");
            writer.write("  \"statistics\": {\n");
            writer.write("    \"languages\": {\n");
            Map<String, Long> languageCount = result.getFileInfoList().stream()
                    .collect(Collectors.groupingBy(FileInfo::getExtension, Collectors.counting()));
            boolean first = true;
            for (Map.Entry<String, Long> entry : languageCount.entrySet()) {
                if (!first) writer.write(",\n");
                String lang = entry.getKey() != null ? entry.getKey() : "unknown";
                writer.write("      \"" + ReportUtils.escapeJson(lang) + "\": " + entry.getValue());
                first = false;
            }
            writer.write("\n    }\n  },\n");
            writer.write("  \"fileTree\": [\n");
            List<FileInfo> files = result.getFileInfoList();
            for (int i = 0; i < files.size(); i++) {
                FileInfo file = files.get(i);
                writer.write("    {\n");
                writer.write("      \"name\": \"" + ReportUtils.escapeJson(file.getName()) + "\",\n");
                writer.write("      \"path\": \"" + ReportUtils.escapeJson(file.getRelativePath()) + "\",\n");
                writer.write("      \"sizeKB\": " + (file.getLength() / 1024) + ",\n");
                writer.write("      \"language\": \"" + ReportUtils.escapeJson(file.getExtension()) + "\",\n");
                writer.write("      \"icon\": \"" + fileIconService.getIcon(file.getExtension()) + "\"\n"); // ИСПРАВЛЕНО: используем fileIconService
                writer.write("    }");
                if (i < files.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("  ]\n");
            writer.write("}\n");
        } catch (IOException e) {
            LOGGER.error("Ошибка при записи JSON метаданных: {}", e.getMessage(), e);
        }
    }
}