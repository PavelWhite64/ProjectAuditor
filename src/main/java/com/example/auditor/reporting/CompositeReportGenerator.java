package com.example.auditor.reporting;

import com.example.auditor.core.ReportGenerator;
import com.example.auditor.core.ReportStrategy;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.utils.ConsoleColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Композитный генератор отчетов, использующий стратегии.
 * Соответствует принципу Open-Closed - новые форматы добавляются через стратегии.
 */
public class CompositeReportGenerator implements ReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeReportGenerator.class);

    private final List<ReportStrategy> reportStrategies;

    public CompositeReportGenerator(List<ReportStrategy> reportStrategies) {
        this.reportStrategies = reportStrategies;
    }

    @Override
    public void generate(AnalysisResult result, AnalysisConfig config, Path outputDir) {
        // Создаем директорию вывода, если не существует
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            LOGGER.error("Ошибка при создании директории вывода: {}", e.getMessage(), e);
            return;
        }

        String outputFileName = config.getOutputFileName();
        List<String> generatedFiles = new ArrayList<>();

        // Генерируем отчеты с использованием стратегий
        for (ReportStrategy strategy : reportStrategies) {
            if (strategy.supports(config.getOutputFormat())) {
                try {
                    strategy.generateReport(result, config, outputDir, outputFileName);
                    String generatedFile = outputDir.resolve(outputFileName + strategy.getFileExtension()).toString();
                    if (Files.exists(Path.of(generatedFile))) {
                        generatedFiles.add(generatedFile);
                        LOGGER.debug("Сгенерирован отчет: {}", generatedFile);
                    }
                } catch (Exception e) {
                    LOGGER.error("Ошибка при генерации отчета стратегией {}: {}",
                            strategy.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        }

        // Выводим информацию о сгенерированных файлах
        printGenerationResults(generatedFiles);

        // Открытие результатов
        if (config.shouldOpenResultsAfterwards()) {
            openGeneratedFiles(generatedFiles);
        }
    }

    private void printGenerationResults(List<String> generatedFiles) {
        System.out.println(ConsoleColors.GREEN + "\n✓ Отчеты успешно сгенерированы! " + ConsoleColors.RESET);
        for (String file : generatedFiles) {
            System.out.println(" • " + file);
        }

        if (generatedFiles.isEmpty()) {
            System.out.println(" • Отчеты не сгенерированы (возможно, не выбран формат вывода)");
        }
    }

    private void openGeneratedFiles(List<String> generatedFiles) {
        System.out.println("  ");
        boolean openNow = readYesNo("Открыть результаты сейчас? ", true);
        if (openNow) {
            for (String filePath : generatedFiles) {
                openFileIfExists(filePath);
            }
        }
    }

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

    private boolean readYesNo(String prompt, boolean defaultYes) {
        System.out.print(prompt + (defaultYes ? " [Y/n]: " : " [y/N]: "));
        String input = new Scanner(System.in).nextLine().trim().toLowerCase();
        if (input.isEmpty()) {
            return defaultYes;
        }
        return input.equals("y") || input.equals("yes");
    }
}