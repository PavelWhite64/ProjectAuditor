package com.example.auditor.reporting;

import com.example.auditor.core.ReportGenerator;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.ConsoleColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ReportGeneratorImpl implements ReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGeneratorImpl.class);

    private final MarkdownReportGenerator markdownGenerator = new MarkdownReportGenerator();
    private final HtmlReportGenerator htmlGenerator = new HtmlReportGenerator();
    private final JsonMetadataGenerator jsonGenerator = new JsonMetadataGenerator();

    @Override
    public void generate(AnalysisResult result, AnalysisConfig config, Path outputDir) {
        String projectName = result.getProjectName();
        String projectType = result.getProjectType();
        List<FileInfo> files = result.getFileInfoList();
        boolean lightMode = config.isLightMode();
        boolean generateJson = config.shouldGenerateJsonMetadata();
        boolean openAfterwards = config.shouldOpenResultsAfterwards();
        String outputFileName = config.getOutputFileName();
        // --- ПЕРЕДАЁМ projectPath ---
        Path projectPath = config.getProjectPath();
        // --- /ПЕРЕДАЁМ projectPath ---

        // Создаем директорию вывода, если не существует
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            LOGGER.error("Ошибка при создании директории вывода: {}", e.getMessage(), e);
            return; // Прерываем генерацию
        }

        // Генерация в зависимости от формата
        AnalysisConfig.OutputFormat format = config.getOutputFormat();

        String markdownFile = null;
        String htmlFile = null;
        String jsonFile = null;

        if (format == AnalysisConfig.OutputFormat.MARKDOWN || format == AnalysisConfig.OutputFormat.BOTH || format == AnalysisConfig.OutputFormat.STRUCTURE_ONLY) {
            markdownFile = outputDir.resolve(outputFileName + ".md").toString();
            // Передаём projectPath в generate
            markdownGenerator.generate(files, projectName, projectType, lightMode, projectPath, markdownFile);
        }

        if (format == AnalysisConfig.OutputFormat.HTML || format == AnalysisConfig.OutputFormat.BOTH) {
            htmlFile = outputDir.resolve(outputFileName + ".html").toString();
            // Передаём projectPath в generate
            htmlGenerator.generate(files, projectName, projectType, lightMode, projectPath, htmlFile);
        }

        if (generateJson) {
            jsonFile = outputDir.resolve(outputFileName + ".json").toString();
            // JsonMetadataGenerator не читает содержимое файлов, передавать projectPath НЕ нужно
            jsonGenerator.generate(result, jsonFile);
        }

        System.out.println(ConsoleColors.GREEN + "\n✓ Отчеты успешно сгенерированы! " + ConsoleColors.RESET);
        System.out.println(" • Markdown: " + (markdownFile != null ? markdownFile : "Не сгенерирован"));
        System.out.println(" • HTML: " + (htmlFile != null ? htmlFile : "Не сгенерирован"));
        System.out.println(" • JSON: " + (jsonFile != null ? jsonFile : "Не сгенерирован"));

        // Открытие результатов (остаётся в основном классе, так как это UI-логика)
        if (openAfterwards) {
            System.out.println("  ");
            boolean openNow = readYesNo("Открыть результаты сейчас? ", true);
            if (openNow) {
                openFileIfExists(markdownFile);
                openFileIfExists(htmlFile);
                openFileIfExists(jsonFile);
            }
        }
    }

    // --- Вспомогательный метод для открытия файлов ---
    private void openFileIfExists(String filePath) {
        if (filePath != null) {
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                try {
                    java.awt.Desktop.getDesktop().open(file);
                    System.out.println(ConsoleColors.GREEN + "✓ Файл открыт: " + filePath + ConsoleColors.RESET);
                } catch (IOException e) {
                    LOGGER.error("Ошибка открытия файла: {}", filePath, e);
                }
            }
        }
    }

    // Метод для получения ответа да/нет (упрощенная версия)
    private boolean readYesNo(String prompt, boolean defaultYes) {
        System.out.print(prompt + (defaultYes ? " [Y/n]: " : " [y/N]: "));
        String input = new java.util.Scanner(System.in).nextLine().trim().toLowerCase();
        if (input.isEmpty()) {
            return defaultYes;
        }
        return input.equals("y") || input.equals("yes");
    }
}