// src/main/java/com/example/auditor/analysis/FileFilterImpl.java
package com.example.auditor.analysis;

import com.example.auditor.config.FilterConfiguration;
import com.example.auditor.core.FileFilter;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.GitIgnoreParser;
import com.example.auditor.utils.PathMatcherUtil; // Предполагаем, что у вас есть этот утилитный класс
import com.example.auditor.utils.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация FileFilter, применяющая фильтры к списку файлов.
 * Использует FilterConfiguration для получения списков паттернов и расширений.
 * <p>
 * Логика фильтрации (в порядке приоритета):
 * 1. Файл исключается, если его размер превышает maxFileSizeKB (если maxFileSizeKB > 0).
 * 2. Файл исключается, если его расширение находится в списке ненужных расширений (из FilterConfiguration).
 * 3. Файл исключается, если он соответствует хотя бы одному паттерну из excludePatterns (из FilterConfiguration).
 * 4. Файл исключается, если он соответствует хотя бы одному паттерну из .gitignore (если используется).
 * 5. Все остальные файлы включаются. Файлы, соответствующие includePatterns (из FilterConfiguration), считаются приоритетными.
 */
public class FileFilterImpl implements FileFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileFilterImpl.class);

    private final FilterConfiguration filterConfig;

    public FileFilterImpl(FilterConfiguration filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public List<FileInfo> filter(List<FileInfo> files, Path projectPath, AnalysisConfig config) {
        List<String> includePatterns = filterConfig.getIncludePatterns(); // Получаем из конфигурации
        List<String> excludePatterns = filterConfig.getExcludePatterns(); // Получаем из конфигурации

        List<String> gitIgnorePatterns = new ArrayList<>();
        if (config.shouldUseGitIgnore()) {
            gitIgnorePatterns = new GitIgnoreParser().parseGitIgnore(projectPath.toString());
        }

        long maxFileSizeBytes = config.getMaxFileSizeKB() > 0 ? config.getMaxFileSizeKB() * 1024L : -1; // -1 означает нет лимита

        List<FileInfo> filteredFiles = new ArrayList<>();
        ProgressBar progressBar = new ProgressBar("Фильтрация файлов", files.size());
        int processed = 0;

        for (FileInfo file : files) {
            progressBar.update(processed++);

            // --- ОТЛАДКА ---
            LOGGER.debug("Processing file: {} (Extension: {}, Size: {} bytes, Type: {})", file.getRelativePath(), file.getExtension(), file.getLength(), file.getType());

            // 1. Проверка размера файла (если установлен лимит)
            if (maxFileSizeBytes > 0 && file.getLength() > maxFileSizeBytes) {
                LOGGER.debug("Excluded by size: {}", file.getRelativePath());
                continue; // Файл слишком большой, исключаем
            }

            // 2. Быстрая проверка расширения файла (до сложных паттернов)
            String extension = getExtension(file.getName()).toLowerCase(); // Приводим к нижнему регистру для сравнения
            LOGGER.debug("Checking extension '{}' for file '{}'", extension, file.getRelativePath());
            LOGGER.debug("Blacklisted extensions: {}", filterConfig.getBlacklistedExtensions());
            if (filterConfig.getBlacklistedExtensions().contains(extension)) {
                LOGGER.debug("Excluded by blacklisted extension: {} (Extension: {})", file.getRelativePath(), extension);
                continue; // Расширение в чёрном списке, исключаем
            }

            // 3. Проверка exclude паттернов (жёсткое исключение, теперь после проверки расширения)
            LOGGER.debug("Checking exclude patterns for file '{}'", file.getRelativePath());
            LOGGER.debug("Exclude patterns: {}", excludePatterns);
            boolean excludeMatch = PathMatcherUtil.matchFile(file.getRelativePath(), excludePatterns);
            if (excludeMatch) {
                LOGGER.debug("Excluded by exclude pattern: {}", file.getRelativePath());
                continue; // Соответствует exclude паттерну - исключаем
            }
            // Блок 'else' с проверкой .git удален.

            // 4. Проверка .gitignore (жёсткое исключение)
            if (PathMatcherUtil.matchFile(file.getRelativePath(), gitIgnorePatterns)) {
                LOGGER.debug("Excluded by .gitignore pattern: {}", file.getRelativePath());
                continue; // Соответствует .gitignore паттерну - исключаем
            }

            // 5. Если файл прошёл все проверки на исключение, добавляем его
            //    и отмечаем как приоритетный, если он соответствует include паттернам.
            boolean isPriority = PathMatcherUtil.matchFile(file.getRelativePath(), includePatterns);
            file.setPriority(isPriority); // Используем сеттер для установки приоритета
            LOGGER.debug("INCLUDED: {} (Priority: {})", file.getRelativePath(), isPriority);
            filteredFiles.add(file);
        }

        progressBar.finish();
        LOGGER.debug("Filtered {} files out of {} total.", filteredFiles.size(), files.size());
        return filteredFiles;
    }

    // --- Вспомогательные методы для получения паттернов и расширения ---

    /**
     * Возвращает расширение файла, включая точку (например, ".java").
     * Если файл не имеет расширения, возвращает пустую строку.
     *
     * @param fileName Имя файла.
     * @return Расширение файла или пустая строка.
     */
    private String getExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex); // Включает точку
        }
        return "";
    }
}