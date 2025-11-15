package com.example.auditor.config;

import com.example.auditor.analysis.DefaultProjectAnalyzer;
import com.example.auditor.analysis.FileFilterImpl;
import com.example.auditor.analysis.FileScannerImpl;
import com.example.auditor.core.*;
import com.example.auditor.reporting.ReportGeneratorImpl;
import com.example.auditor.ui.InteractivePrompter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Реализация фабрики компонентов с явным созданием зависимостей.
 */
public class DefaultComponentFactory implements ComponentFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComponentFactory.class);

    private final FilterConfiguration filterConfiguration;
    private Scanner scanner;

    public DefaultComponentFactory() {
        this.filterConfiguration = loadFilterConfiguration();
    }

    @Override
    public ProjectScanner createProjectScanner() {
        LOGGER.debug("Creating ProjectScanner (FileScannerImpl)");
        return new FileScannerImpl();
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
        LOGGER.debug("Creating ReportGenerator (ReportGeneratorImpl)");
        return new ReportGeneratorImpl();
    }

    @Override
    public UserInterface createUserInterface() {
        LOGGER.debug("Creating UserInterface (InteractivePrompter)");
        return new InteractivePrompter(getOrCreateScanner());
    }

    private Scanner getOrCreateScanner() {
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        return scanner;
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