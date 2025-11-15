package com.example.auditor.analysis;

import com.example.auditor.config.FilterConfiguration;
import com.example.auditor.core.FileFilter;
import com.example.auditor.core.ProgressIndicator;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.ConsoleProgressIndicator;
import com.example.auditor.utils.GitIgnoreParser;
import com.example.auditor.utils.PathMatcherUtil;
import com.example.auditor.utils.FileExtensionUtils;
import com.example.auditor.utils.FileExtensionUtils.ExtensionFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация FileFilter, применяющая фильтры к списку файлов.
 * Использует FilterConfiguration для получения списков паттернов и расширений.
 */
public class FileFilterImpl implements FileFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileFilterImpl.class);

    private final FilterConfiguration filterConfig;

    public FileFilterImpl(FilterConfiguration filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public List<FileInfo> filter(List<FileInfo> files, Path projectPath, AnalysisConfig config) {
        List<String> includePatterns = filterConfig.getIncludePatterns();
        List<String> excludePatterns = filterConfig.getExcludePatterns();

        List<String> gitIgnorePatterns = new ArrayList<>();
        if (config.shouldUseGitIgnore()) {
            gitIgnorePatterns = new GitIgnoreParser().parseGitIgnore(projectPath.toString());
        }

        long maxFileSizeBytes = config.getMaxFileSizeKB() > 0 ? config.getMaxFileSizeKB() * 1024L : -1;

        List<FileInfo> filteredFiles = new ArrayList<>();
        // ИСПРАВЛЕНО: Используем ProgressIndicator вместо ProgressBar
        ProgressIndicator progressIndicator = new ConsoleProgressIndicator("Фильтрация файлов", files.size());
        int processed = 0;

        for (FileInfo file : files) {
            progressIndicator.update(processed++);

            LOGGER.debug("Processing file: {} (Extension: {}, Size: {} bytes, Type: {})",
                    file.getRelativePath(), file.getExtension(), file.getLength(), file.getType());

            // 1. Проверка размера файла (если установлен лимит)
            if (maxFileSizeBytes > 0 && file.getLength() > maxFileSizeBytes) {
                LOGGER.debug("Excluded by size: {}", file.getRelativePath());
                continue;
            }

            // 2. Быстрая проверка расширения файла (до сложных паттернов)
            String extension = FileExtensionUtils.getExtension(file.getName(), ExtensionFormat.WITH_DOT);
            LOGGER.debug("Checking extension '{}' for file '{}'", extension, file.getRelativePath());

            if (filterConfig.getBlacklistedExtensions().contains(extension)) {
                LOGGER.debug("Excluded by blacklisted extension: {} (Extension: {})", file.getRelativePath(), extension);
                continue;
            }

            // 3. Проверка exclude паттернов (жёсткое исключение)
            boolean excludeMatch = PathMatcherUtil.matchFile(file.getRelativePath(), excludePatterns);
            if (excludeMatch) {
                LOGGER.debug("Excluded by exclude pattern: {}", file.getRelativePath());
                continue;
            }

            // 4. Проверка .gitignore (жёсткое исключение)
            if (PathMatcherUtil.matchFile(file.getRelativePath(), gitIgnorePatterns)) {
                LOGGER.debug("Excluded by .gitignore pattern: {}", file.getRelativePath());
                continue;
            }

            // 5. Если файл прошёл все проверки на исключение, добавляем его
            boolean isPriority = PathMatcherUtil.matchFile(file.getRelativePath(), includePatterns);
            file.setPriority(isPriority);
            LOGGER.debug("INCLUDED: {} (Priority: {})", file.getRelativePath(), isPriority);
            filteredFiles.add(file);
        }

        progressIndicator.finish();
        LOGGER.debug("Filtered {} files out of {} total.", filteredFiles.size(), files.size());
        return filteredFiles;
    }
}