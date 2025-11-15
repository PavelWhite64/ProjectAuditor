package com.example.auditor.reporting;

import com.example.auditor.core.FileIconService;
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

public class HtmlReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportGenerator.class);

    private final FileIconService fileIconService;

    public HtmlReportGenerator(FileIconService fileIconService) {
        this.fileIconService = fileIconService;
    }

    // Метод generate теперь принимает ограничения для чтения файлов
    public void generate(List<FileInfo> files, String projectName, String projectType, boolean lightMode,
                         Path projectPath, String outputFile, long maxContentSizeBytes, int maxLinesPerFile) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            String currentDate = ReportUtils.getCurrentDate();
            long totalSizeKB = files.stream().mapToLong(FileInfo::getLength).sum() / 1024;
            int totalFiles = files.size();

            writer.write("<!DOCTYPE html>\n<html lang=\"ru\">\n<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>Аудит проекта: " + ReportUtils.escapeHtml(projectName) + "</title>\n");
            writer.write("<style>\n");
            writer.write("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
            writer.write(".header { background-color: #007acc; color: white; padding: 15px; border-radius: 5px; }\n");
            writer.write(".section { margin: 20px 0; background-color: white; padding: 15px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
            writer.write(".warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 10px; margin: 10px 0; border-radius: 3px; }\n");
            writer.write("pre { background-color: #f4f4f4; padding: 10px; overflow-x: auto; border-radius: 3px; }\n");
            writer.write("code { background-color: #f4f4f4; padding: 2px 4px; border-radius: 3px; }\n");
            writer.write("</style>\n");
            writer.write("</head>\n<body>\n");

            writer.write("<div class=\"header\">\n");
            writer.write("<h1>Аудит проекта: " + ReportUtils.escapeHtml(projectName) + "</h1>\n");
            writer.write("<p><strong>Сгенерировано:</strong> " + currentDate + "</p>\n");
            writer.write("<p><strong>Файлов включено:</strong> " + totalFiles + "</p>\n");
            writer.write("<p><strong>Общий размер:</strong> " + totalSizeKB + " KB</p>\n");
            writer.write("<p><strong>Тип проекта:</strong> " + ReportUtils.escapeHtml(projectType) + "</p>\n");
            writer.write("<p><strong>Режим:</strong> " + (lightMode ? "Light" : "Full") + "</p>\n");
            writer.write("<p><strong>Ограничения:</strong> Макс. размер содержимого: " + (maxContentSizeBytes / 1024 / 1024) + " MB, Макс. строк: " + maxLinesPerFile + "</p>\n");
            writer.write("</div>\n");

            writer.write("<div class=\"section\">\n<h2>Статистика проекта</h2>\n");
            Map<String, Long> languageCount = files.stream()
                    .collect(Collectors.groupingBy(FileInfo::getExtension, Collectors.counting()));
            writer.write("<h3>Распределение по языкам</h3><ul>\n");
            for (Map.Entry<String, Long> entry : languageCount.entrySet()) {
                String lang = entry.getKey() != null ? entry.getKey() : "unknown";
                writer.write("<li><strong>" + ReportUtils.escapeHtml(lang) + ":</strong> " + entry.getValue() + " файлов</li>\n");
            }
            writer.write("</ul>\n</div>\n");

            writer.write("<div class=\"section\">\n<h2>Структура проекта</h2>\n<pre>\n" + ReportUtils.escapeHtml(ReportUtils.generateTreeMarkdown(files, fileIconService)) + "</pre>\n</div>\n");

            if (!lightMode) {
                writer.write("<div class=\"section\">\n<h2>Содержимое файлов</h2>\n");
                for (FileInfo file : files) {
                    String icon = fileIconService.getIcon(file.getExtension());
                    String language = fileIconService.getLanguage(file.getExtension());
                    double kb = file.getLength() / 1024.0;

                    // Добавляем предупреждение для больших файлов
                    if (file.getLength() > maxContentSizeBytes) {
                        writer.write("<div class=\"warning\">\n");
                        writer.write("<strong>Примечание:</strong> Файл слишком большой (" + String.format(Locale.US, "%.1f", kb) + " KB). Содержимое не будет прочитано полностью.\n");
                        writer.write("</div>\n");
                    } else if (kb > 50) {
                        writer.write("<div class=\"warning\">\n");
                        writer.write("<strong>Примечание:</strong> Файл большого размера (" + String.format(Locale.US, "%.1f", kb) + " KB). Может быть обрезан.\n");
                        writer.write("</div>\n");
                    }

                    writer.write("<h3>" + icon + " " + ReportUtils.escapeHtml(file.getRelativePath()) + " (" + String.format(Locale.US, "%.1f", kb) + " KB)</h3>\n");
                    writer.write("<pre><code class=\"" + ReportUtils.escapeHtml(language) + "\">\n");
                    try {
                        // Используем ограничения при чтении содержимого файла
                        String content = ReportUtils.readFileContent(file.getFullName(), projectPath, maxContentSizeBytes, maxLinesPerFile);
                        writer.write(ReportUtils.escapeHtml(content));
                    } catch (IOException e) {
                        writer.write("<!-- Ошибка чтения файла: " + ReportUtils.escapeHtml(e.getMessage()) + " -->");
                        LOGGER.error("Ошибка чтения файла {}: {}", file.getRelativePath(), e.getMessage());
                    }
                    writer.write("\n</code></pre>\n");
                }
                writer.write("</div>\n");
            }

            writer.write("<div class=\"section\">\n<h2>Итоги</h2>\n");
            writer.write("<ul>\n<li><strong>Всего файлов:</strong> " + files.size() + "</li>\n");
            writer.write("<li><strong>Общий размер:</strong> " + totalSizeKB + " KB</li>\n");
            writer.write("<li><strong>Тип проекта:</strong> " + ReportUtils.escapeHtml(projectType) + "</li>\n");
            writer.write("<li><strong>Режим:</strong> " + (lightMode ? "Light" : "Full") + "</li>\n");
            writer.write("<li><strong>Ограничения чтения:</strong> " + (maxContentSizeBytes / 1024 / 1024) + " MB, " + maxLinesPerFile + " строк</li>\n");
            writer.write("<li><strong>Сгенерировано:</strong> " + currentDate + "</li>\n");
            writer.write("</ul>\n");
            writer.write("<blockquote><strong>ВАЖНО:</strong> Сфокусируйся на критических проблемах безопасности!</blockquote>\n");
            writer.write("</div>\n");

            writer.write("</body>\n</html>");

        } catch (IOException e) {
            LOGGER.error("Ошибка при записи HTML отчета: {}", e.getMessage(), e);
        }
    }
}