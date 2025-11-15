package com.example.auditor.reporting;

import com.example.auditor.model.AnalysisResult;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.FileIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path; // Добавлен импорт Path
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class HtmlReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportGenerator.class);

    // Метод generate теперь принимает Path projectPath
    public void generate(List<FileInfo> files, String projectName, String projectType, boolean lightMode, Path projectPath, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            String currentDate = ReportUtils.getCurrentDate();
            long totalSizeKB = files.stream().mapToLong(FileInfo::getLength).sum() / 1024;
            int totalFiles = files.size();

            writer.write("<!DOCTYPE html>\n<html lang=\"ru\">\n<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>Аудит проекта: " + ReportUtils.escapeHtml(projectName) + "</title>\n");
            writer.write("<style>\n"); // Простая стилизация
            writer.write("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
            writer.write(".header { background-color: #007acc; color: white; padding: 15px; border-radius: 5px; }\n");
            writer.write(".section { margin: 20px 0; background-color: white; padding: 15px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
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

            writer.write("<div class=\"section\">\n<h2>Структура проекта</h2>\n<pre>\n" + ReportUtils.escapeHtml(ReportUtils.generateTreeMarkdown(files)) + "</pre>\n</div>\n");

            if (!lightMode) {
                writer.write("<div class=\"section\">\n<h2>Содержимое файлов</h2>\n");
                for (FileInfo file : files) {
                    String icon = FileIcon.getIcon(file.getExtension());
                    String language = FileIcon.getLanguage(file.getExtension());
                    double kb = file.getLength() / 1024.0;
                    writer.write("<h3>" + icon + " " + ReportUtils.escapeHtml(file.getRelativePath()) + " (" + String.format("%.1f", kb) + " KB)</h3>\n");
                    writer.write("<pre><code class=\"" + ReportUtils.escapeHtml(language) + "\">\n");
                    try {
                        // Используем обновлённый метод readFileContent с проверкой безопасности
                        String content = ReportUtils.escapeHtml(ReportUtils.readFileContent(file.getFullName(), projectPath)).trim();
                        writer.write(content);
                    } catch (IOException e) {
                        writer.write(" <!-- Ошибка чтения файла --> ");
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
            writer.write("<li><strong>Сгенерировано:</strong> " + currentDate + "</li>\n");
            writer.write("</ul>\n");
            writer.write("<blockquote><strong>ВАЖНО:</strong> Сфокусируйся на критических проблемах безопасности!</blockquote>\n");
            writer.write("</div>\n");

            writer.write("</body>\n</html>");

        } catch (IOException e) {
            LOGGER.error("Ошибка при записи HTML отчета: {}", e.getMessage(), e); // Логируем с трейсом
        }
    }
}