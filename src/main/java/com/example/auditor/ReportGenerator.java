package com.example.auditor;

import com.example.auditor.utils.ProgressBar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportGenerator {

    public void generateMarkdownReport(
            List<FileScanner.FileInfo> files,
            String projectPath,
            String projectName,
            String projectType,
            boolean lightMode,
            String outputFile
    ) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(outputFile),
                        StandardCharsets.UTF_8
                )
        )) {
            // Метаданные
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            int totalSizeKB = files.stream().mapToInt(f -> (int)(f.length / 1024)).sum();

            writer.write("# Аудит проекта: " + escapeMarkdown(projectName) + "\n");
            writer.write("**Сгенерировано:** " + currentDate + "  \n");
            writer.write("**Путь:** `" + escapeMarkdown(projectPath) + "`  \n");
            writer.write("**Файлов включено:** " + files.size() + "  \n");
            writer.write("**Общий размер:** " + totalSizeKB + " KB  \n");
            writer.write("**Тип проекта:** " + escapeMarkdown(projectType) + "  \n");
            writer.write("**Режим:** " + (lightMode ? "Light" : "Full") + "  \n");
            writer.write("**Безопасность:** Чувствительные файлы исключены автоматически\n");
            writer.write("---\n");

            // Статистика проекта
            writer.write("## Статистика проекта\n");
            writer.write("### Распределение по языкам\n");

            Map<String, Integer> languageStats = getLanguageStats(files);
            languageStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            writer.write("  - **" + escapeMarkdown(entry.getKey()) + ":** " + entry.getValue() + " файлов\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            writer.write("\n### Распределение по размерам\n");
            Map<String, Integer> sizeStats = getSizeStats(files);
            for (Map.Entry<String, Integer> entry : sizeStats.entrySet()) {
                writer.write("  - **" + escapeMarkdown(entry.getKey()) + ":** " + entry.getValue() + " файлов\n");
            }

            // Структура проекта
            writer.write("\n## Структура проекта\n");
            writer.write("```\n");
            writer.write(projectName + "/\n");

            // Генерация дерева каталогов - ИСПРАВЛЕНО
            Set<String> directories = new TreeSet<>();
            for (FileScanner.FileInfo file : files) {
                String[] parts = file.relativePath.split("/");
                // ИСПРАВЛЕНО: проверка на пустой путь
                if (parts.length > 1) {
                    for (int i = 0; i < parts.length - 1; i++) {
                        String dirPath = String.join("/", Arrays.copyOfRange(parts, 0, i + 1)) + "/";
                        directories.add(dirPath);
                    }
                }
            }

            // Вывод директорий
            for (String dir : directories) {
                int depth = dir.split("/").length - 1;
                String indent = "  ".repeat(depth);
                String[] dirParts = dir.split("/");
                // ИСПРАВЛЕНО: безопасное получение имени директории
                String dirName = dirParts.length > 1 ? dirParts[dirParts.length - 2] : dirParts[0];
                writer.write(indent + "[DIR] " + dirName + "\n");
            }

            // Вывод файлов
            for (FileScanner.FileInfo file : files) {
                int depth = file.relativePath.split("/").length - 1;
                if (depth < 0) depth = 0;
                String indent = "  ".repeat(depth);
                String icon = FileIcon.getIcon(file.name);
                writer.write(indent + icon + " " + file.name + "\n");
            }

            writer.write("```\n");

            // Список файлов
            writer.write("\n## Список файлов (" + files.size() + ")\n");
            for (FileScanner.FileInfo file : files) {
                double kb = file.length / 1024.0;
                String icon = FileIcon.getIcon(file.name);
                writer.write("  - " + icon + " `" + escapeMarkdown(file.relativePath) + "` (" + String.format("%.1f", kb) + " KB)\n");
            }

            // Содержимое файлов (если не Light режим)
            if (!lightMode) {
                writer.write("\n## Содержимое файлов\n");

                ProgressBar progressBar = new ProgressBar("Обработка содержимого", files.size());
                int processed = 0;

                for (FileScanner.FileInfo file : files) {
                    progressBar.update(processed++);

                    try {
                        String content = readFileContent(file.fullName);
                        if (content != null && !content.trim().isEmpty()) {
                            String language = ProjectAnalyzer.getLanguageFromExtension(file.name);
                            String icon = FileIcon.getIcon(file.name);

                            // Предупреждение для больших файлов
                            String warning = "";
                            if (file.length > 50 * 1024) { // 50KB
                                double sizeKB = file.length / 1024.0;
                                warning = "> **Примечание:** Файл большого размера (" + String.format("%.0f", sizeKB) + " KB). LLM может пропустить часть контента.  \n\n";
                            }

                            writer.write("\n" + warning + "### " + icon + " " + escapeMarkdown(file.relativePath) + "\n");
                            writer.write("```" + language + "\n");
                            writer.write(content.trim() + "\n");
                            writer.write("```\n");
                        }
                    } catch (Exception e) {
                        System.err.println("Не удалось прочитать файл " + file.relativePath + ": " + e.getMessage());
                    }
                }

                progressBar.finish();
            }

            // Чек-листы и инструкции
            writer.write("\n## Детальные чек-листы для аудита\n");
            writer.write("### Безопасность\n");
            writer.write("- [ ] **SQL-инъекции:** Проверить использование параметризованных запросов\n");
            writer.write("- [ ] **XSS:** Проверить экранирование пользовательского ввода\n");
            writer.write("- [ ] **CSRF:** Проверить наличие токенов CSRF\n");
            writer.write("- [ ] **Аутентификация:** Проверить надежность механизмов входа\n");
            writer.write("- [ ] **Авторизация:** Проверить контроль доступа\n");
            writer.write("- [ ] **Конфиденциальность данных:** Проверить маскировку в логах\n");
            writer.write("- [ ] **Безопасные заголовки:** Проверить CSP, HSTS\n");

            writer.write("\n### Качество кода  \n");
            writer.write("- [ ] **Обработка ошибок:** Проверить корректность try-catch блоков\n");
            writer.write("- [ ] **Валидация данных:** Проверить проверку пользовательского ввода\n");
            writer.write("- [ ] **Сложность кода:** Выявить слишком сложные методы\n");
            writer.write("- [ ] **Дублирование:** Найти повторяющийся код\n");

            writer.write("\n### Производительность\n");
            writer.write("- [ ] **N+1 запросы:** Проверить эффективность БД\n");
            writer.write("- [ ] **Кэширование:** Проверить использование кэша\n");
            writer.write("- [ ] **Алгоритмы:** Выявить неоптимальные алгоритмы\n");
            writer.write("- [ ] **Размер сборки:** Проанализировать артефакты\n");

            writer.write("\n## Инструкции для LLM\n");
            writer.write("> **ВАЖНО: Сфокусируйся на критических проблемах безопасности!**\n");
            writer.write("### Стратегия аудита:\n");
            writer.write("1. **Контекстный анализ** - учитывай тип проекта (" + escapeMarkdown(projectType) + ")\n");
            writer.write("2. **Приоритет безопасности** - сначала критические уязвимости  \n");
            writer.write("3. **Конкретные примеры** - указывай файлы, строки и предлагай исправления\n");
            writer.write("4. **Практические рекомендации** - покажи как исправить\n");

            writer.write("\n### Формат ответа:\n");
            writer.write("- **КРИТИЧЕСКИ** - уязвимости безопасности\n");
            writer.write("- **ВЫСОКИЙ ПРИОРИТЕТ** - серьезные проблемы качества  \n");
            writer.write("- **РЕКОМЕНДАЦИЯ** - улучшения архитектуры\n");
            writer.write("- **СОВЕТ** - необязательные улучшения\n");

            writer.write("\n---\n");
            writer.write("## Итоги\n");
            writer.write("- **Всего файлов:** " + files.size() + "\n");
            writer.write("- **Общий размер:** " + totalSizeKB + " KB  \n");
            writer.write("- **Тип проекта:** " + escapeMarkdown(projectType) + "\n");
            writer.write("- **Режим:** " + (lightMode ? "Light" : "Full") + "\n");
            writer.write("- **Сгенерировано:** " + currentDate + "\n");
            writer.write("> Проект **" + escapeMarkdown(projectName) + "** готов для глубокого аудита! Удачи!\n");
            writer.write("*Сгенерировано с помощью Project Auditor v1.0*\n");

        } catch (IOException e) {
            System.err.println("Ошибка генерации Markdown отчета: " + e.getMessage());
        }
    }

    public void generateHtmlReport(
            List<FileScanner.FileInfo> files,
            String projectPath,
            String projectName,
            String projectType,
            boolean lightMode,
            String outputFile
    ) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(outputFile),
                        StandardCharsets.UTF_8
                )
        )) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            int totalSizeKB = files.stream().mapToInt(f -> (int)(f.length / 1024)).sum();

            writer.write("<!DOCTYPE html>\n");
            writer.write("<html lang=\"ru\">\n");
            writer.write("<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            writer.write("    <title>Аудит проекта: " + escapeHtml(projectName) + "</title>\n");
            writer.write("    <style>\n");
            writer.write("        body { \n");
            writer.write("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; \n");
            writer.write("            line-height: 1.6; \n");
            writer.write("            margin: 0; \n");
            writer.write("            padding: 20px; \n");
            writer.write("            background-color: #f5f5f5; \n");
            writer.write("            color: #333; \n");
            writer.write("        }\n");
            writer.write("        .container { \n");
            writer.write("            max-width: 1200px; \n");
            writer.write("            margin: 0 auto; \n");
            writer.write("            background: white; \n");
            writer.write("            padding: 30px; \n");
            writer.write("            border-radius: 10px; \n");
            writer.write("            box-shadow: 0 2px 10px rgba(0,0,0,0.1); \n");
            writer.write("        }\n");
            writer.write("        h1 { \n");
            writer.write("            color: #2c3e50; \n");
            writer.write("            border-bottom: 3px solid #3498db; \n");
            writer.write("            padding-bottom: 10px; \n");
            writer.write("        }\n");
            writer.write("        h2 { \n");
            writer.write("            color: #34495e; \n");
            writer.write("            margin-top: 30px; \n");
            writer.write("        }\n");
            writer.write("        h3 { \n");
            writer.write("            color: #16a085; \n");
            writer.write("        }\n");
            writer.write("        .metadata { \n");
            writer.write("            background: #ecf0f1; \n");
            writer.write("            padding: 15px; \n");
            writer.write("            border-radius: 5px; \n");
            writer.write("            margin: 20px 0; \n");
            writer.write("        }\n");
            writer.write("        .statistics { \n");
            writer.write("            display: grid; \n");
            writer.write("            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); \n");
            writer.write("            gap: 20px; \n");
            writer.write("            margin: 20px 0; \n");
            writer.write("        }\n");
            writer.write("        .stat-card { \n");
            writer.write("            background: #f8f9fa; \n");
            writer.write("            padding: 15px; \n");
            writer.write("            border-radius: 5px; \n");
            writer.write("            border-left: 4px solid #3498db; \n");
            writer.write("        }\n");
            writer.write("        .file-tree { \n");
            writer.write("            background: #2c3e50; \n");
            writer.write("            color: #ecf0f1; \n");
            writer.write("            padding: 15px; \n");
            writer.write("            border-radius: 5px; \n");
            writer.write("            font-family: 'Courier New', monospace; \n");
            writer.write("            white-space: pre; \n");
            writer.write("            overflow-x: auto; \n");
            writer.write("        }\n");
            writer.write("        .file-list { \n");
            writer.write("            list-style: none; \n");
            writer.write("            padding: 0; \n");
            writer.write("        }\n");
            writer.write("        .file-item { \n");
            writer.write("            padding: 5px 0; \n");
            writer.write("            border-bottom: 1px solid #ecf0f1; \n");
            writer.write("        }\n");
            writer.write("        .checklist { \n");
            writer.write("            background: #fff3cd; \n");
            writer.write("            padding: 15px; \n");
            writer.write("            border-radius: 5px; \n");
            writer.write("            border-left: 4px solid #ffc107; \n");
            writer.write("        }\n");
            writer.write("        code { \n");
            writer.write("            background: #2c3e50; \n");
            writer.write("            color: #ecf0f1; \n");
            writer.write("            padding: 2px 6px; \n");
            writer.write("            border-radius: 3px; \n");
            writer.write("        }\n");
            writer.write("        pre { \n");
            writer.write("            background: #2c3e50; \n");
            writer.write("            color: #ecf0f1; \n");
            writer.write("            padding: 15px; \n");
            writer.write("            border-radius: 5px; \n");
            writer.write("            overflow-x: auto; \n");
            writer.write("        }\n");
            writer.write("        .language-badge { \n");
            writer.write("            background: #3498db; \n");
            writer.write("            color: white; \n");
            writer.write("            padding: 2px 8px; \n");
            writer.write("            border-radius: 12px; \n");
            writer.write("            font-size: 0.8em; \n");
            writer.write("            margin-right: 10px; \n");
            writer.write("        }\n");
            writer.write("    </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("    <div class=\"container\">\n");
            writer.write("        <h1>🔍 Аудит проекта: " + escapeHtml(projectName) + "</h1>\n");
            writer.write("        <div class=\"metadata\">\n");
            writer.write("            <strong>Сгенерировано:</strong> " + currentDate + "<br>\n");
            writer.write("            <strong>Путь:</strong> <code>" + escapeHtml(projectPath) + "</code><br>\n");
            writer.write("            <strong>Файлов включено:</strong> " + files.size() + "<br>\n");
            writer.write("            <strong>Общий размер:</strong> " + totalSizeKB + " KB<br>\n");
            writer.write("            <strong>Тип проекта:</strong> " + escapeHtml(projectType) + "<br>\n");
            writer.write("            <strong>Режим:</strong> " + (lightMode ? "Light" : "Full") + "<br>\n");
            writer.write("            <strong>Безопасность:</strong> Чувствительные файлы исключены автоматически\n");
            writer.write("        </div>\n");

            // Статистика
            writer.write("        <h2>📊 Статистика проекта</h2>\n");
            writer.write("        <div class=\"statistics\">\n");
            writer.write("            <div class=\"stat-card\">\n");
            writer.write("                <h3>Распределение по языкам</h3>\n");
            writer.write("                <ul>\n");

            Map<String, Integer> languageStats = getLanguageStats(files);
            languageStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            writer.write("                    <li><strong>" + escapeHtml(entry.getKey()) + ":</strong> " + entry.getValue() + " файлов</li>\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            writer.write("                </ul>\n");
            writer.write("            </div>\n");
            writer.write("            <div class=\"stat-card\">\n");
            writer.write("                <h3>Распределение по размерам</h3>\n");
            writer.write("                <ul>\n");

            Map<String, Integer> sizeStats = getSizeStats(files);
            for (Map.Entry<String, Integer> entry : sizeStats.entrySet()) {
                writer.write("                    <li><strong>" + escapeHtml(entry.getKey()) + ":</strong> " + entry.getValue() + " файлов</li>\n");
            }

            writer.write("                </ul>\n");
            writer.write("            </div>\n");
            writer.write("        </div>\n");

            // Структура проекта
            writer.write("        <h2>📁 Структура проекта</h2>\n");
            writer.write("        <div class=\"file-tree\">\n");
            writer.write(escapeHtml(projectName) + "/\n");

            // Генерация дерева каталогов
            Set<String> directories = new TreeSet<>();
            for (FileScanner.FileInfo file : files) {
                String[] parts = file.relativePath.split("/");
                for (int i = 0; i < parts.length - 1; i++) {
                    String dirPath = String.join("/", Arrays.copyOfRange(parts, 0, i + 1)) + "/";
                    directories.add(dirPath);
                }
            }

            // Вывод директорий
            for (String dir : directories) {
                int depth = dir.split("/").length - 1;
                String indent = "  ".repeat(depth);
                String[] dirParts = dir.split("/");
                String dirName = dirParts.length > 1 ? dirParts[dirParts.length - 2] : dirParts[0];
                writer.write(indent + "📁 " + escapeHtml(dirName) + "\n");
            }

            // Вывод файлов
            for (FileScanner.FileInfo file : files) {
                int depth = file.relativePath.split("/").length - 1;
                if (depth < 0) depth = 0;
                String indent = "  ".repeat(depth);
                String icon = FileIcon.getHtmlIcon(file.name);
                writer.write(indent + icon + " " + escapeHtml(file.name) + "\n");
            }

            writer.write("        </div>\n");

            // Список файлов
            writer.write("        <h2>📄 Список файлов (" + files.size() + ")</h2>\n");
            writer.write("        <ul class=\"file-list\">\n");

            for (FileScanner.FileInfo file : files) {
                double kb = file.length / 1024.0;
                String icon = FileIcon.getHtmlIcon(file.name);
                writer.write("            <li class=\"file-item\">" + icon + " <code>" + escapeHtml(file.relativePath) + "</code> (" + String.format("%.1f", kb) + " KB)</li>\n");
            }

            writer.write("        </ul>\n");

            // Чек-листы
            writer.write("        <div class=\"checklist\">\n");
            writer.write("            <h2>✅ Чек-листы для аудита</h2>\n");
            writer.write("            <h3>🔒 Безопасность</h3>\n");
            writer.write("            <ul>\n");
            writer.write("                <li><input type=\"checkbox\"> <strong>SQL-инъекции:</strong> Проверить использование параметризованных запросов</li>\n");
            writer.write("                <li><input type=\"checkbox\"> <strong>XSS:</strong> Проверить экранирование пользовательского ввода</li>\n");
            writer.write("                <li><input type=\"checkbox\"> <strong>CSRF:</strong> Проверить наличие токенов CSRF</li>\n");
            writer.write("                <li><input type=\"checkbox\"> <strong>Аутентификация:</strong> Проверить надежность механизмов входа</li>\n");
            writer.write("                <li><input type=\"checkbox\"> <strong>Авторизация:</strong> Проверить контроль доступа</li>\n");
            writer.write("            </ul>\n");
            writer.write("            <h3>📝 Качество кода</h3>\n");
            writer.write("            <ul>\n");
            writer.write("                <li><input type=\"checkbox\"> <strong>Обработка ошибок:</strong> Проверить корректность try-catch блоков</li>\n");
            writer.write("                <li><input type=\"checkbox\"> <strong>Валидация данных:</strong> Проверить проверку пользовательского ввода</li>\n");
            writer.write("                <li><input type=\"checkbox\"> <strong>Сложность кода:</strong> Выявить слишком сложные методы</li>\n");
            writer.write("                <li><input type=\"checkbox\"> <strong>Дублирование:</strong> Найти повторяющийся код</li>\n");
            writer.write("            </ul>\n");
            writer.write("        </div>\n");

            // Инструкции для LLM
            writer.write("        <h2>🤖 Инструкции для LLM</h2>\n");
            writer.write("        <blockquote>\n");
            writer.write("            <strong>ВАЖНО: Сфокусируйся на критических проблемах безопасности!</strong>\n");
            writer.write("        </blockquote>\n");
            writer.write("        <h3>Стратегия аудита:</h3>\n");
            writer.write("        <ol>\n");
            writer.write("            <li><strong>Контекстный анализ</strong> - учитывай тип проекта (" + escapeHtml(projectType) + ")</li>\n");
            writer.write("            <li><strong>Приоритет безопасности</strong> - сначала критические уязвимости</li>\n");
            writer.write("            <li><strong>Конкретные примеры</strong> - указывай файлы, строки и предлагай исправления</li>\n");
            writer.write("            <li><strong>Практические рекомендации</strong> - покажи как исправить</li>\n");
            writer.write("        </ol>\n");

            writer.write("        <hr>\n");
            writer.write("        <div style=\"text-align: center; margin-top: 40px; color: #7f8c8d; font-size: 0.9em;\">\n");
            writer.write("            <p><strong>Итоги анализа:</strong></p>\n");
            writer.write("            <p>📊 Всего файлов: " + files.size() + " | 💾 Общий размер: " + totalSizeKB + " KB | 🏷️ Тип проекта: " + escapeHtml(projectType) + "</p>\n");
            writer.write("            <p>Проект <strong>" + escapeHtml(projectName) + "</strong> готов для глубокого аудита! Удачи! 🚀</p>\n");
            writer.write("            <p><em>Сгенерировано с помощью Project Auditor v1.0</em></p>\n");
            writer.write("        </div>\n");
            writer.write("    </div>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");

        } catch (IOException e) {
            System.err.println("Ошибка генерации HTML отчета: " + e.getMessage());
        }
    }

    public void generateJsonReport(
            List<FileScanner.FileInfo> files,
            String projectPath,
            String projectName,
            String projectType,
            String outputFile
    ) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(outputFile),
                        StandardCharsets.UTF_8
                )
        )) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            int totalSizeKB = files.stream().mapToInt(f -> (int)(f.length / 1024)).sum();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject root = new JsonObject();

            // Метаданные
            JsonObject metadata = new JsonObject();
            metadata.addProperty("projectName", projectName);
            metadata.addProperty("projectPath", projectPath);
            metadata.addProperty("generated", currentDate);
            metadata.addProperty("totalFiles", files.size());
            metadata.addProperty("totalSizeKB", totalSizeKB);
            metadata.addProperty("projectType", projectType);
            metadata.addProperty("auditMode", "Full");
            root.add("metadata", metadata);

            // Статистика
            JsonObject statistics = new JsonObject();

            JsonObject languages = new JsonObject();
            getLanguageStats(files).forEach(languages::addProperty);
            statistics.add("languages", languages);

            JsonObject fileSizes = new JsonObject();
            getSizeStats(files).forEach(fileSizes::addProperty);
            statistics.add("fileSizes", fileSizes);

            root.add("statistics", statistics);

            // Файлы
            JsonArray fileArray = new JsonArray();
            for (FileScanner.FileInfo file : files) {
                JsonObject fileInfo = new JsonObject();
                fileInfo.addProperty("name", file.name);
                fileInfo.addProperty("path", file.relativePath);
                fileInfo.addProperty("sizeKB", file.length / 1024.0);
                fileInfo.addProperty("language", ProjectAnalyzer.getLanguageFromExtension(file.name));
                fileInfo.addProperty("icon", FileIcon.getIcon(file.name));
                fileArray.add(fileInfo);
            }
            root.add("fileTree", fileArray);

            writer.write(gson.toJson(root));

        } catch (IOException e) {
            System.err.println("Ошибка генерации JSON отчета: " + e.getMessage());
        }
    }

    private Map<String, Integer> getLanguageStats(List<FileScanner.FileInfo> files) {
        Map<String, Integer> stats = new HashMap<>();
        for (FileScanner.FileInfo file : files) {
            String lang = ProjectAnalyzer.getLanguageFromExtension(file.name);
            stats.put(lang, stats.getOrDefault(lang, 0) + 1);
        }
        return stats;
    }

    private Map<String, Integer> getSizeStats(List<FileScanner.FileInfo> files) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("0-1KB", 0);
        stats.put("1-10KB", 0);
        stats.put("10-100KB", 0);
        stats.put("100-500KB", 0);
        stats.put("500KB+", 0);

        for (FileScanner.FileInfo file : files) {
            double sizeKB = file.length / 1024.0;
            if (sizeKB < 1) {
                stats.put("0-1KB", stats.get("0-1KB") + 1);
            } else if (sizeKB < 10) {
                stats.put("1-10KB", stats.get("1-10KB") + 1);
            } else if (sizeKB < 100) {
                stats.put("10-100KB", stats.get("10-100KB") + 1);
            } else if (sizeKB < 500) {
                stats.put("100-500KB", stats.get("100-500KB") + 1);
            } else {
                stats.put("500KB+", stats.get("500KB+") + 1);
            }
        }

        return stats;
    }

    // Вспомогательный метод для экранирования Markdown
    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("`", "\\`");
    }

    // Вспомогательный метод для экранирования HTML
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "<")
                .replace(">", ">")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String readFileContent(String filePath) throws IOException {
        try {
            // Попытка прочитать как текст
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Если не удалось прочитать (бинарный файл и т.д.)
            return null;
        }
    }
}