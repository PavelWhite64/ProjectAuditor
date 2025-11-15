package com.example.auditor.config;

import com.example.auditor.analysis.DefaultProjectAnalyzer;
import com.example.auditor.analysis.FileFilterImpl;
import com.example.auditor.analysis.FileScannerImpl;
import com.example.auditor.core.*;
import com.example.auditor.reporting.*;
import com.example.auditor.ui.InteractivePrompter;
import com.example.auditor.utils.ConsoleProgressIndicator;
import com.example.auditor.utils.DefaultFileIconService;
import com.example.auditor.utils.DefaultFileTypeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация фабрики компонентов с явным созданием зависимостей.
 * Теперь создает InteractivePrompter с правильной кодировкой.
 */
public class DefaultComponentFactory implements ComponentFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComponentFactory.class);

    private final FilterConfiguration filterConfiguration;

    public DefaultComponentFactory() {
        this.filterConfiguration = loadFilterConfiguration();
    }

    @Override
    public ProjectScanner createProjectScanner() {
        LOGGER.debug("Creating ProjectScanner (FileScannerImpl)");
        FileTypeClassifier fileTypeClassifier = createFileTypeClassifier();
        return new FileScannerImpl(fileTypeClassifier);
    }

    @Override
    public FileFilter createFileFilter() {
        LOGGER.debug("Creating FileFilter (FileFilterImpl)");
        return new FileFilterImpl(filterConfiguration);
    }

    @Override
    public ProjectAnalyzer createProjectAnalyzer() {
        LOGGER.debug("Creating ProjectAnalyzer (DefaultProjectAnalyzer)");
        ProjectScanner scanner = createProjectScanner();
        FileFilter filter = createFileFilter();
        return new DefaultProjectAnalyzer(scanner, filter);
    }

    @Override
    public ReportGenerator createReportGenerator() {
        LOGGER.debug("Creating CompositeReportGenerator with strategies");
        FileIconService fileIconService = createFileIconService();

        List<ReportStrategy> strategies = new ArrayList<>();
        strategies.add(new MarkdownReportStrategy(fileIconService));
        strategies.add(new HtmlReportStrategy(fileIconService));
        strategies.add(new JsonReportStrategy(fileIconService));

        return new CompositeReportGenerator(strategies);
    }

    @Override
    public UserInterface createUserInterface() {
        LOGGER.debug("Creating UserInterface (InteractivePrompter)");
        // Теперь передаем System.in напрямую, кодировка устанавливается в конструкторе InteractivePrompter
        return new InteractivePrompter(System.in);
    }

    /**
     * Создает индикатор прогресса
     */
    public ProgressIndicator createProgressIndicator(String taskName, int totalSteps) {
        LOGGER.debug("Creating ProgressIndicator for task: {}", taskName);
        return new ConsoleProgressIndicator(taskName, totalSteps);
    }

    /**
     * Создает классификатор типов файлов
     */
    public FileTypeClassifier createFileTypeClassifier() {
        LOGGER.debug("Creating FileTypeClassifier");
        return new DefaultFileTypeClassifier();
    }

    /**
     * Создает сервис иконок файлов
     */
    public FileIconService createFileIconService() {
        LOGGER.debug("Creating FileIconService");
        return new DefaultFileIconService();
    }

    private FilterConfiguration loadFilterConfiguration() {
        LOGGER.debug("Loading FilterConfiguration from resource");
        try {
            return JsonFilterConfiguration.loadFromJsonResource("/filter-config.json");
        } catch (Exception e) {
            LOGGER.error("Failed to load FilterConfiguration: {}", e.getMessage(), e);
            throw new RuntimeException("Cannot start without FilterConfiguration", e);
        }
    }
}