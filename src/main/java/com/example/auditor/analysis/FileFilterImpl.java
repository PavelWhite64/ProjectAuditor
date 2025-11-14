// src/main/java/com/example/auditor/analysis/FileFilterImpl.java
package com.example.auditor.analysis;

import com.example.auditor.core.FileFilter;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.GitIgnoreParser; // Убедитесь, что GitIgnoreParser находится тут или импорт верен
import com.example.auditor.utils.PathMatcherUtil;
import com.example.auditor.utils.ProgressBar;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация FileFilter, применяющая фильтры к списку файлов.
 * Теперь exclude и gitIgnore паттерны определяют жестко исключаемые файлы.
 */
public class FileFilterImpl implements FileFilter {

    @Override
    public List<FileInfo> filter(List<FileInfo> files, Path projectPath, AnalysisConfig config) {
        List<String> includePatterns = getIncludePatterns(); // Список приоритетных паттернов
        List<String> excludePatterns = getExcludePatterns(); // Список жестко исключаемых паттернов
        List<String> gitIgnorePatterns = new ArrayList<>();

        if (config.shouldUseGitIgnore()) {
            // Исправление: передаем projectPath.toString()
            gitIgnorePatterns = new GitIgnoreParser().parseGitIgnore(projectPath.toString());
        }

        long maxFileSizeKB = config.getMaxFileSizeKB(); // Теперь это 0, если ограничений нет
        List<FileInfo> filteredFiles = new ArrayList<>();
        ProgressBar progressBar = new ProgressBar("Фильтрация файлов", files.size());
        int processed = 0;

        // --- ОТЛАДКА: Выведем несколько файлов и результаты матчинга ---
        int debugPrinted = 0; // Счетчик для первых N файлов
        final int debugCount = 5; // Сколько файлов отладить
        // --- /ОТЛАДКА ---

        for (FileInfo file : files) {
            progressBar.update(processed++);

            // Проверка размера файла: если maxFileSizeKB == 0, ограничений нет
            // Теперь логика безопасна от переполнения
            if (maxFileSizeKB > 0) {
                long maxSizeBytes = maxFileSizeKB * 1024L; // Умножаем ТОЛЬКО если > 0
                if (file.getLength() > maxSizeBytes) {
                    // Если файл больше лимита и лимит > 0, пропускаем
                    continue;
                }
            }
            // Если maxFileSizeKB == 0, проверка не выполняется, файл проходит дальше

            // --- ОТЛАДКА: Проверим первые N файлов ---
            if (debugPrinted < debugCount) { // Проверим первые N файлов
                System.out.println("DEBUG: Проверка файла: " + file.getRelativePath());
                System.out.println("DEBUG: Длина файла: " + file.getLength());
                System.out.println("DEBUG: Расширение файла: " + file.getExtension());

                // Проверка exclude паттернов (жёсткое исключение)
                boolean excludeMatch = PathMatcherUtil.matchFile(file.getRelativePath(), excludePatterns);
                System.out.println("DEBUG: Exclude match: " + excludeMatch + " для паттернов: " + excludePatterns);

                if (excludeMatch) {
                    System.out.println("DEBUG: Файл " + file.getRelativePath() + " исключён по exclude паттерну.");
                    debugPrinted++; // Увеличиваем только если файл исключён
                    continue; // Переходим к следующему файлу, не проверяя gitignore
                }

                // Проверка .gitignore (жёсткое исключение)
                boolean gitIgnoreMatch = PathMatcherUtil.matchFile(file.getRelativePath(), gitIgnorePatterns);
                System.out.println("DEBUG: GitIgnore match: " + gitIgnoreMatch + " для паттернов: " + gitIgnorePatterns);

                if (gitIgnoreMatch) {
                    System.out.println("DEBUG: Файл " + file.getRelativePath() + " исключён по .gitignore.");
                    debugPrinted++; // Увеличиваем только если файл исключён
                    continue; // Переходим к следующему файлу
                }
                debugPrinted++; // Увеличиваем, если файл не исключён
                System.out.println("---");
            }
            // --- /ОТЛАДКА ---


            // Проверка exclude паттернов (жёсткое исключение)
            boolean excludeMatch = PathMatcherUtil.matchFile(file.getRelativePath(), excludePatterns);
            if (excludeMatch) {
                //System.out.println("DEBUG: Файл " + file.getRelativePath() + " прошёл Exclude проверку.");
                continue; // Соответствует exclude паттерну - исключаем
            }

            // Проверка .gitignore (жёсткое исключение)
            boolean gitIgnoreMatch = PathMatcherUtil.matchFile(file.getRelativePath(), gitIgnorePatterns);
            if (gitIgnoreMatch) {
                //System.out.println("DEBUG: Файл " + file.getRelativePath() + " прошёл GitIgnore проверку.");
                continue; // Соответствует .gitignore паттерну - исключаем
            }

            // Если файл не исключён, он проходит фильтрацию
            // (Файлы, соответствующие include, также проходят, так как не исключены)
            filteredFiles.add(file);
        }
        progressBar.finish();
        System.out.println("DEBUG: Всего файлов до фильтрации: " + files.size());
        System.out.println("DEBUG: Всего файлов после фильтрации: " + filteredFiles.size());
        return filteredFiles;
    }

    // --- Вспомогательные методы для получения паттернов ---
    // Эти списки можно хранить в отдельном конфигурационном файле
    private List<String> getIncludePatterns() {
        // Эти паттерны определяют ПРИОРИТЕТНЫЕ файлы, которые включаются, если не исключены
        return List.of(
                "**/*.java", "**/*.kt", "**/*.scala", "**/*.groovy", "**/*.py", "**/*.js", "**/*.ts",
                "**/*.go", "**/*.rs", "**/*.cpp", "**/*.c", "**/*.h", "**/*.hpp", "**/*.cs",
                "**/*.php", "**/*.rb", "**/*.pl", "**/*.pm", "**/*.sql", "**/*.xml", "**/*.json",
                "**/*.yaml", "**/*.yml", "**/*.properties", "**/*.cfg", "**/*.conf", "**/*.ini",
                "**/*.sh", "**/*.bat", "**/*.cmd", "**/*.ps1", "**/*.sql", "**/*.txt", "**/*.md",
                "**/*.html", "**/*.htm", "**/*.css", "**/*.scss", "**/*.less", "**/*.vue", "**/*.svelte",
                "**/Dockerfile", "**/docker-compose.yml", "**/Makefile", "**/CMakeLists.txt", "**/build.gradle",
                "**/pom.xml", "**/package.json", "**/requirements.txt", "**/setup.py", "**/go.mod",
                "**/*.feature", "**/*.robot", "**/*.test", "**/*test*", "**/*spec*"
        );
    }

    private List<String> getExcludePatterns() {
        return List.of(
                // Системные и служебные
                ".git/**", ".svn/**", ".hg/**", "CVS/**", // Исправлено: .git/** вместо **/.git/**
                // IDE
                ".idea/**", "*.iml", "*.ipr", "*.iws", "nbproject/**", "nbactions.xml",
                ".vs/**", ".vscode/**", "*.swp", "*.swo", "._*", // Добавлен **/._* для Mac
                // Бинарные и артефакты
                "target/**", "build/**", "dist/**", "out/**", "bin/**", "obj/**", // Исправлено
                "*.jar", "*.war", "*.ear", "*.zip", "*.rar", "*.tar.gz", "*.so",
                "*.dll", "*.exe", "*.class", "*.o", "*.a", "*.lib", "*.dylib",
                "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.ico", "*.svg",
                "*.pdf", "*.doc", "*.docx", "*.xls", "*.xlsx", "*.ppt", "*.pptx",
                // Логи и временные файлы
                "logs/**", "*.log", "tmp/**", "temp/**", "*.tmp", "*.temp",
                // Node.js
                "node_modules/**", "npm-debug.log*", "yarn-debug.log*", "yarn-error.log*",
                // Python
                "__pycache__/**", "*.pyc", "*.pyo", "*.pyd", ".pytest_cache/**", ".coverage",
                // Go
                "vendor/**", // Хотя vendor может быть интересен, часто исключается
                // .NET
                "packages/**", "TestResults/**", "*.nupkg", "*.snupkg",
                // Другие
                "*secret*", "*password*", "*credential*", "*.key", "*.pem", "*.crt",
                "*.cert", "*.pfx", "*.property", // config.property исключается через паттерн
                "application-*.yml", "application-*.yaml", "application-*.properties",
                "local.*", "dev.*", "prod.*", "test.*", ".env*"
        );
    }
}