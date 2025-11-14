// src/main/java/com/example/auditor/reporting/ReportGeneratorImpl.java
package com.example.auditor.reporting;

import com.example.auditor.core.ReportGenerator;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.ConsoleColors;
import com.example.auditor.utils.FileIcon; // Убедитесь, что FileIcon находится тут или импорт верен

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация ReportGenerator, генерирующая отчеты в различных форматах.
 */
public class ReportGeneratorImpl implements ReportGenerator {

    @Override
    public void generate(AnalysisResult result, AnalysisConfig config, Path outputDir) {
        String projectName = result.getProjectName();
        String projectType = result.getProjectType();
        List<FileInfo> files = result.getFileInfoList();
        boolean lightMode = config.isLightMode();
        boolean generateJson = config.shouldGenerateJsonMetadata();
        boolean openAfterwards = config.shouldOpenResultsAfterwards();
        String outputFileName = config.getOutputFileName();

        // Создаем директорию вывода, если не существует
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            System.err.println("Ошибка при создании директории вывода: " + e.getMessage());
            e.printStackTrace();
            return; // Прерываем генерацию
        }

        // Генерация в зависимости от формата
        AnalysisConfig.OutputFormat format = config.getOutputFormat();

        String markdownFile = null;
        String htmlFile = null;
        String jsonFile = null;

        if (format == AnalysisConfig.OutputFormat.MARKDOWN || format == AnalysisConfig.OutputFormat.BOTH || format == AnalysisConfig.OutputFormat.STRUCTURE_ONLY) {
            markdownFile = outputDir.resolve(outputFileName + ".md").toString();
            generateMarkdownReport(files, projectName, projectType, lightMode, markdownFile);
        }

        if (format == AnalysisConfig.OutputFormat.HTML || format == AnalysisConfig.OutputFormat.BOTH) {
            htmlFile = outputDir.resolve(outputFileName + ".html").toString();
            generateHtmlReport(files, projectName, projectType, lightMode, htmlFile);
        }

        if (generateJson) {
            jsonFile = outputDir.resolve(outputFileName + ".json").toString();
            generateJsonMetadata(result, jsonFile);
        }

        System.out.println(ConsoleColors.GREEN + "\n✓ Отчеты успешно сгенерированы!" + ConsoleColors.RESET);
        System.out.println(" • Markdown: " + (markdownFile != null ? markdownFile : "Не сгенерирован"));
        System.out.println(" • HTML: " + (htmlFile != null ? htmlFile : "Не сгенерирован"));
        System.out.println(" • JSON: " + (jsonFile != null ? jsonFile : "Не сгенерирован"));

        // Открытие результатов
        if (openAfterwards) {
            System.out.println(" ");
            boolean openNow = readYesNo("Открыть результаты сейчас? ", true);
            if (openNow) {
                if (markdownFile != null && new java.io.File(markdownFile).exists()) {
                    try {
                        java.awt.Desktop.getDesktop().open(new java.io.File(markdownFile));
                        System.out.println(ConsoleColors.GREEN + "✓ Markdown файл открыт." + ConsoleColors.RESET);
                    } catch (IOException e) {
                        System.err.println("Ошибка открытия Markdown файла: " + e.getMessage());
                    }
                }
                if (htmlFile != null && new java.io.File(htmlFile).exists()) {
                    try {
                        java.awt.Desktop.getDesktop().open(new java.io.File(htmlFile));
                        System.out.println(ConsoleColors.GREEN + "✓ HTML файл открыт." + ConsoleColors.RESET);
                    } catch (IOException e) {
                        System.err.println("Ошибка открытия HTML файла: " + e.getMessage());
                    }
                }
                if (jsonFile != null && new java.io.File(jsonFile).exists()) {
                    try {
                        java.awt.Desktop.getDesktop().open(new java.io.File(jsonFile));
                        System.out.println(ConsoleColors.GREEN + "✓ JSON файл открыт." + ConsoleColors.RESET);
                    } catch (IOException e) {
                        System.err.println("Ошибка открытия JSON файла: " + e.getMessage());
                    }
                }
            }
        }
    }

    // --- Генерация Markdown ---
    private void generateMarkdownReport(List<FileInfo> files, String projectName, String projectType, boolean lightMode, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            long totalSizeKB = files.stream().mapToLong(FileInfo::getLength).sum() / 1024;
            int totalFiles = files.size();

            writer.write("# Аудит проекта: " + escapeMarkdown(projectName) + "\n\n");
            writer.write("**Сгенерировано:** " + currentDate + "\n");
            writer.write("**Файлов включено:** " + totalFiles + "\n");
            writer.write("**Общий размер:** " + totalSizeKB + " KB\n");
            writer.write("**Тип проекта:** " + escapeMarkdown(projectType) + "\n");
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
            writer.write("```\n"); // Начинаем блок кода для дерева
            writer.write(generateTreeMarkdown(files)); // Вызываем улучшенный метод
            writer.write("```\n"); // Заканчиваем блок кода

            // Содержимое файлов (если не Light режим)
            if (!lightMode) {
                writer.write("\n## Содержимое файлов\n");
                int processed = 0;
                for (FileInfo file : files) {
                    String icon = FileIcon.getIcon(file.getExtension());
                    String language = FileIcon.getLanguage(file.getExtension());
                    long kb = file.getLength() / 1024L;
                    String warning = "";
                    if (kb > 50) { // Пример: предупреждение для файлов > 50KB
                        warning = " > **Примечание:** Файл большого размера (" + String.format("%.0f", (double) file.getLength() / 1024.0) + " KB). LLM может пропустить часть контента.\n\n";
                    }
                    writer.write("\n" + warning + "### " + icon + " " + escapeMarkdown(file.getRelativePath()) + " (`" + String.format("%.1f", (double) file.getLength() / 1024.0) + " KB`)\n");
                    writer.write("```" + language + "\n");
                    try {
                        String content = readFileContent(file.getFullName());
                        writer.write(content.trim() + "\n");
                    } catch (IOException e) {
                        writer.write("<!-- Ошибка чтения файла -->\n");
                    }
                    writer.write("```\n");
                }
            }

            // Итоги
            writer.write("\n---\n");
            writer.write("## Итоги\n");
            writer.write("- **Всего файлов:** " + files.size() + "\n");
            writer.write("- **Общий размер:** " + totalSizeKB + " KB\n");
            writer.write("- **Тип проекта:** " + escapeMarkdown(projectType) + "\n");
            writer.write("- **Режим:** " + (lightMode ? "Light" : "Full") + "\n");
            writer.write("- **Сгенерировано:** " + currentDate + "\n");
            writer.write(" > Проект **" + escapeMarkdown(projectName) + "** готов для анализа LLM.\n");
            writer.write(" > ВАЖНО: Сфокусируйся на критических проблемах безопасности!\n");

        } catch (IOException e) {
            System.err.println("Ошибка при записи Markdown отчета: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Улучшенная генерация дерева ---
    private String generateTreeMarkdown(List<FileInfo> files) {
        StringBuilder tree = new StringBuilder();
        // Сортируем файлы по пути для корректного построения дерева
        List<String> sortedPaths = files.stream()
                .map(FileInfo::getRelativePath)
                .sorted()
                .collect(Collectors.toList());

        // Используем Map для отслеживания уже добавленных директорий
        java.util.Set<String> addedDirs = new java.util.HashSet<>();
        java.util.Set<String> addedFiles = new java.util.HashSet<>();

        for (String path : sortedPaths) {
            String[] parts = path.split("/");
            StringBuilder currentPath = new StringBuilder();

            // Добавляем директории
            for (int i = 0; i < parts.length - 1; i++) { // -1, чтобы не включать файл
                currentPath.append(parts[i]).append("/");
                String dirPath = currentPath.toString();
                if (!addedDirs.contains(dirPath)) {
                    String indent = "  ".repeat(i); // Отступы
                    tree.append(indent).append("📁 ").append(parts[i]).append("\n");
                    addedDirs.add(dirPath);
                }
            }

            // Добавляем файл
            String fileName = parts[parts.length - 1];
            String parentDirPath = currentPath.toString();
            String fullPath = parentDirPath + fileName;
            if (!addedFiles.contains(fullPath)) {
                String indent = "  ".repeat(parts.length - 1); // Отступы для файла
                String icon = FileIcon.getIcon(fileName); // Иконка для файла
                tree.append(indent).append(icon).append(" ").append(fileName).append("\n");
                addedFiles.add(fullPath);
            }
        }
        return tree.toString();
    }

    // --- Генерация HTML (упрощенный пример) ---
    private void generateHtmlReport(List<FileInfo> files, String projectName, String projectType, boolean lightMode, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            long totalSizeKB = files.stream().mapToLong(FileInfo::getLength).sum() / 1024;
            int totalFiles = files.size();

            writer.write("<!DOCTYPE html>\n<html lang=\"ru\">\n<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>Аудит проекта: " + escapeHtml(projectName) + "</title>\n");
            writer.write("<style>\n"); // Простая стилизация
            writer.write("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
            writer.write(".header { background-color: #007acc; color: white; padding: 15px; border-radius: 5px; }\n");
            writer.write(".section { margin: 20px 0; background-color: white; padding: 15px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
            writer.write("pre { background-color: #f4f4f4; padding: 10px; overflow-x: auto; border-radius: 3px; }\n");
            writer.write("code { background-color: #f4f4f4; padding: 2px 4px; border-radius: 3px; }\n");
            writer.write("</style>\n");
            writer.write("</head>\n<body>\n");

            writer.write("<div class=\"header\">\n");
            writer.write("<h1>Аудит проекта: " + escapeHtml(projectName) + "</h1>\n");
            writer.write("<p><strong>Сгенерировано:</strong> " + currentDate + "</p>\n");
            writer.write("<p><strong>Файлов включено:</strong> " + totalFiles + "</p>\n");
            writer.write("<p><strong>Общий размер:</strong> " + totalSizeKB + " KB</p>\n");
            writer.write("<p><strong>Тип проекта:</strong> " + escapeHtml(projectType) + "</p>\n");
            writer.write("<p><strong>Режим:</strong> " + (lightMode ? "Light" : "Full") + "</p>\n");
            writer.write("</div>\n");

            writer.write("<div class=\"section\">\n<h2>Статистика проекта</h2>\n");
            Map<String, Long> languageCount = files.stream()
                    .collect(Collectors.groupingBy(FileInfo::getExtension, Collectors.counting()));
            writer.write("<h3>Распределение по языкам</h3><ul>\n");
            for (Map.Entry<String, Long> entry : languageCount.entrySet()) {
                String lang = entry.getKey() != null ? entry.getKey() : "unknown";
                writer.write("<li><strong>" + escapeHtml(lang) + ":</strong> " + entry.getValue() + " файлов</li>\n");
            }
            writer.write("</ul>\n</div>\n");

            writer.write("<div class=\"section\">\n<h2>Структура проекта</h2>\n<pre>\n" + escapeHtml(generateTreeMarkdown(files)) + "</pre>\n</div>\n"); // Используем улучшенное дерево

            if (!lightMode) {
                writer.write("<div class=\"section\">\n<h2>Содержимое файлов</h2>\n");
                for (FileInfo file : files) {
                    String icon = FileIcon.getIcon(file.getExtension());
                    String language = FileIcon.getLanguage(file.getExtension());
                    double kb = file.getLength() / 1024.0;
                    writer.write("<h3>" + icon + " " + escapeHtml(file.getRelativePath()) + " (" + String.format("%.1f", kb) + " KB)</h3>\n");
                    writer.write("<pre><code class=\"" + escapeHtml(language) + "\">\n");
                    try {
                        String content = escapeHtml(readFileContent(file.getFullName())).trim();
                        writer.write(content);
                    } catch (IOException e) {
                        writer.write("<!-- Ошибка чтения файла -->");
                    }
                    writer.write("\n</code></pre>\n");
                }
                writer.write("</div>\n");
            }

            writer.write("<div class=\"section\">\n<h2>Итоги</h2>\n");
            writer.write("<ul>\n<li><strong>Всего файлов:</strong> " + files.size() + "</li>\n");
            writer.write("<li><strong>Общий размер:</strong> " + totalSizeKB + " KB</li>\n");
            writer.write("<li><strong>Тип проекта:</strong> " + escapeHtml(projectType) + "</li>\n");
            writer.write("<li><strong>Режим:</strong> " + (lightMode ? "Light" : "Full") + "</li>\n");
            writer.write("<li><strong>Сгенерировано:</strong> " + currentDate + "</li>\n");
            writer.write("</ul>\n");
            writer.write("<blockquote><strong>ВАЖНО:</strong> Сфокусируйся на критических проблемах безопасности!</blockquote>\n");
            writer.write("</div>\n");

            writer.write("</body>\n</html>");

        } catch (IOException e) {
            System.err.println("Ошибка при записи HTML отчета: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Генерация JSON ---
    private void generateJsonMetadata(AnalysisResult result, String outputFile) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            writer.write("{\n");
            writer.write("  \"metadata\": {\n");
            writer.write("    \"projectName\": \"" + escapeJson(result.getProjectName()) + "\",\n");
            writer.write("    \"totalFiles\": " + result.getTotalFiles() + ",\n");
            writer.write("    \"totalSizeKB\": " + result.getTotalSizeKB() + ",\n");
            writer.write("    \"projectType\": \"" + escapeJson(result.getProjectType()) + "\",\n");
            writer.write("    \"generatedAt\": \"" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\"\n");
            writer.write("  },\n");
            writer.write("  \"statistics\": {\n");
            writer.write("    \"languages\": {\n");
            Map<String, Long> languageCount = result.getFileInfoList().stream()
                    .collect(Collectors.groupingBy(FileInfo::getExtension, Collectors.counting()));
            boolean first = true;
            for (Map.Entry<String, Long> entry : languageCount.entrySet()) {
                if (!first) writer.write(",\n");
                String lang = entry.getKey() != null ? entry.getKey() : "unknown";
                writer.write("      \"" + escapeJson(lang) + "\": " + entry.getValue());
                first = false;
            }
            writer.write("\n    }\n  },\n");
            writer.write("  \"fileTree\": [\n");
            List<FileInfo> files = result.getFileInfoList();
            for (int i = 0; i < files.size(); i++) {
                FileInfo file = files.get(i);
                writer.write("    {\n");
                writer.write("      \"name\": \"" + escapeJson(file.getName()) + "\",\n");
                writer.write("      \"path\": \"" + escapeJson(file.getRelativePath()) + "\",\n");
                writer.write("      \"sizeKB\": " + (file.getLength() / 1024) + ",\n");
                writer.write("      \"language\": \"" + escapeJson(file.getExtension()) + "\",\n");
                writer.write("      \"icon\": \"" + FileIcon.getIcon(file.getExtension()) + "\"\n"); // Иконка как строка
                writer.write("    }");
                if (i < files.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("  ]\n");
            writer.write("}\n");
        } catch (IOException e) {
            System.err.println("Ошибка при записи JSON метаданных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Вспомогательные методы ---
    private String readFileContent(Path filePath) throws IOException {
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    private String escapeMarkdown(String input) {
        if (input == null) return "";
        // Простое экранирование часто используемых символов Markdown
        return input.replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("`", "\\`")
                .replace("#", "\\#")
                .replace("<", "<")
                .replace(">", ">");
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "<")
                .replace(">", ">")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Метод для получения ответа да/нет (упрощенная версия, можно перенести в отдельный класс или использовать Scanner из InteractivePrompter)
    private boolean readYesNo(String prompt, boolean defaultYes) {
        System.out.print(prompt + (defaultYes ? " [Y/n]: " : " [y/N]: "));
        String input = new java.util.Scanner(System.in).nextLine().trim().toLowerCase();
        if (input.isEmpty()) {
            return defaultYes;
        }
        return input.equals("y") || input.equals("yes");
    }
}