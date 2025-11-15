package com.example.auditor.reporting;

import com.example.auditor.core.FileIconService;
import com.example.auditor.core.FileSystem;
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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MarkdownReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownReportGenerator.class);

    private final FileIconService fileIconService;
    private final FileSystem fileSystem;

    public MarkdownReportGenerator(FileIconService fileIconService, FileSystem fileSystem) {
        this.fileIconService = fileIconService;
        this.fileSystem = fileSystem;
    }

    // Метод generate теперь принимает ограничения для чтения файлов
    public void generate(List<FileInfo> files, String projectName, String projectType, boolean lightMode,
                         Path projectPath, String outputFile, long maxContentSizeBytes, int maxLinesPerFile) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            String currentDate = ReportUtils.getCurrentDate();
            long totalSizeKB = files.stream().mapToLong(FileInfo::getLength).sum() / 1024;
            int totalFiles = files.size();

            writer.write("# Аудит проекта: " + ReportUtils.escapeMarkdown(projectName) + "\n\n");
            writer.write("**Сгенерировано:** " + currentDate + "\n");
            writer.write("**Файлов включено:** " + totalFiles + "\n");
            writer.write("**Общий размер:** " + totalSizeKB + " KB\n");
            writer.write("**Тип проекта:** " + ReportUtils.escapeMarkdown(projectType) + "\n");
            writer.write("**Режим:** " + (lightMode ? "Light" : "Full") + "\n");
            writer.write("**Ограничения:** Макс. размер содержимого: " + (maxContentSizeBytes / 1024 / 1024) + " MB, Макс. строк: " + maxLinesPerFile + "\n\n");

            // Статистика
            writer.write("## Статистика проекта\n\n");
            Map<String, Long> languageCount = files.stream()
                    .collect(Collectors.groupingBy(FileInfo::getExtension, Collectors.counting()));
            writer.write("### Распределение по языкам\n");
            for (Map.Entry<String, Long> entry : languageCount.entrySet()) {
                String lang = entry.getKey() != null ? entry.getKey() : "unknown";
                writer.write("- **" + lang + ":** " + entry.getValue() + " файлов\n");
            }
            writer.write("\n");

            // Структура
            writer.write("## Структура проекта\n");
            writer.write("```\n");
            writer.write(ReportUtils.generateTreeMarkdown(files, fileIconService));
            writer.write("```\n");

            // Содержимое файлов (если не Light режим)
            if (!lightMode) {
                writer.write("\n## Содержимое файлов\n");
                for (FileInfo file : files) {
                    String icon = fileIconService.getIcon(file.getExtension());
                    String language = fileIconService.getLanguage(file.getExtension());
                    double kb = file.getLength() / 1024.0;

                    String warning = "";
                    if (file.getLength() > maxContentSizeBytes) {
                        warning = "  > **Примечание:** Файл слишком большой (" + String.format(Locale.US, "%.1f", kb) + " KB). Содержимое не будет прочитано полностью.\n\n";
                    } else if (kb > 50) {
                        warning = "  > **Примечание:** Файл большого размера (" + String.format(Locale.US, "%.1f", kb) + " KB). Может быть обрезан.\n\n";
                    }

                    writer.write("\n" + warning + "### " + icon + " " + escapeMarkdown(file.getRelativePath()) + " (`" + String.format(Locale.US, "%.1f", kb) + " KB`)\n");
                    writer.write("```" + language + "\n");
                    try {
                        // Используем ограничения при чтении содержимого файла с FileSystem
                        String content = ReportUtils.readFileContent(file.getFullName(), projectPath, maxContentSizeBytes, maxLinesPerFile, fileSystem);
                        writer.write(content.trim() + "\n");
                    } catch (IOException e) {
                        writer.write("<!-- Ошибка чтения файла: " + e.getMessage() + " -->\n");
                        LOGGER.error("Ошибка чтения файла {}: {}", file.getRelativePath(), e.getMessage());
                    }
                    writer.write("```\n");
                }
            }

            // Итоги
            writer.write("\n---\n");
            writer.write("## Итоги\n");
            writer.write("- **Всего файлов:** " + files.size() + "\n");
            writer.write("- **Общий размер:** " + totalSizeKB + " KB\n");
            writer.write("- **Тип проекта:** " + ReportUtils.escapeMarkdown(projectType) + "\n");
            writer.write("- **Режим:** " + (lightMode ? "Light" : "Full") + "\n");
            writer.write("- **Ограничения чтения:** " + (maxContentSizeBytes / 1024 / 1024) + " MB, " + maxLinesPerFile + " строк\n");
            writer.write("- **Сгенерировано:** " + currentDate + "\n");
            writer.write("  > Проект **" + ReportUtils.escapeMarkdown(projectName) + "** готов для анализа LLM.\n");
            writer.write("  > ВАЖНО: Сфокусируйся на критических проблемах безопасности!\n");

        } catch (IOException e) {
            LOGGER.error("Ошибка при записи Markdown отчета: {}", e.getMessage(), e);
        }
    }

    // Вспомогательный метод для экранирования
    private String escapeMarkdown(String input) {
        return ReportUtils.escapeMarkdown(input);
    }
}