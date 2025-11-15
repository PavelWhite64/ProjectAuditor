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
import java.util.Map;
import java.util.stream.Collectors;

public class MarkdownReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownReportGenerator.class);

    // Метод generate теперь принимает Path projectPath
    public void generate(List<FileInfo> files, String projectName, String projectType, boolean lightMode, Path projectPath, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            String currentDate = ReportUtils.getCurrentDate();
            long totalSizeKB = files.stream().mapToLong(FileInfo::getLength).sum() / 1024;
            int totalFiles = files.size();

            writer.write("# Аудит проекта: " + ReportUtils.escapeMarkdown(projectName) + "\n\n");
            writer.write("**Сгенерировано:** " + currentDate + "\n");
            writer.write("**Файлов включено:** " + totalFiles + "\n");
            writer.write("**Общий размер:** " + totalSizeKB + " KB\n");
            writer.write("**Тип проекта:** " + ReportUtils.escapeMarkdown(projectType) + "\n");
            writer.write("**Режим:** " + (lightMode ? "Light" : "Full") + "\n\n");

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
            writer.write(ReportUtils.generateTreeMarkdown(files));
            writer.write("```\n");

            // Содержимое файлов (если не Light режим)
            if (!lightMode) {
                writer.write("\n## Содержимое файлов\n");
                for (FileInfo file : files) {
                    String icon = FileIcon.getIcon(file.getExtension());
                    String language = FileIcon.getLanguage(file.getExtension());
                    double kb = file.getLength() / 1024.0;
                    String warning = "";
                    if (kb > 50) { // Пример: предупреждение для файлов > 50KB
                        warning = "  > **Примечание:** Файл большого размера (" + String.format("%.0f", kb) + " KB). LLM может пропустить часть контента.\n\n";
                    }
                    writer.write("\n" + warning + "### " + icon + " " + escapeMarkdown(file.getRelativePath()) + " (`" + String.format("%.1f", kb) + " KB`)\n");
                    writer.write("```" + language + "\n");
                    try {
                        // Используем обновлённый метод readFileContent с проверкой безопасности
                        String content = ReportUtils.readFileContent(file.getFullName(), projectPath);
                        writer.write(content.trim() + "\n");
                    } catch (IOException e) {
                        writer.write(" <!-- Ошибка чтения файла -->\n");
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
            writer.write("- **Сгенерировано:** " + currentDate + "\n");
            writer.write("  > Проект **" + ReportUtils.escapeMarkdown(projectName) + "** готов для анализа LLM.\n");
            writer.write("  > ВАЖНО: Сфокусируйся на критических проблемах безопасности!\n");

        } catch (IOException e) {
            LOGGER.error("Ошибка при записи Markdown отчета: {}", e.getMessage(), e); // Логируем с трейсом
        }
    }

    // Вспомогательный метод для экранирования (может быть статическим в ReportUtils, но оставим тут для примера)
    private String escapeMarkdown(String input) {
        return ReportUtils.escapeMarkdown(input);
    }
}