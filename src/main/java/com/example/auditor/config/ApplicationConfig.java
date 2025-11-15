package com.example.auditor.config;

import com.example.auditor.analysis.DefaultProjectAnalyzer;
import com.example.auditor.analysis.FileFilterImpl;
import com.example.auditor.analysis.FileScannerImpl;
import com.example.auditor.core.ProjectAnalyzer;
import com.example.auditor.core.ProjectScanner;
import com.example.auditor.core.FileFilter;
import com.example.auditor.core.ReportGenerator;
import com.example.auditor.core.UserInterface;
import com.example.auditor.reporting.ReportGeneratorImpl;
import com.example.auditor.ui.InteractivePrompter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.util.Scanner;

/**
 * Класс конфигурации приложения, отвечающий за создание и настройку зависимостей.
 */
public class ApplicationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    // Поле для Scanner
    private Scanner scanner;

    // Метод для получения Scanner (создаётся один раз)
    private Scanner getScanner() {
        if (scanner == null) {
            Console console = System.console();
            if (console != null) {
                // Если Console доступен, можно использовать его, но Scanner создаётся из System.in
                // System.console() не всегда работает корректно с Gradle, так что используем System.in
                // LOGGER.debug("Using Console for input");
                // scanner = new Scanner(console.reader()); // Альтернатива, но может быть нестабильной
            }
            if (scanner == null) {
                // LOGGER.debug("Console not available, using Scanner(System.in)");
                scanner = new Scanner(System.in);
            }
        }
        return scanner;
    }

    // Метод для получения экземпляра UserInterface
    public UserInterface getUserInterface() {
        LOGGER.debug("Creating UserInterface (InteractivePrompter) with Scanner");
        Scanner sc = getScanner(); // Получаем один и тот же экземпляр Scanner
        return new InteractivePrompter(sc); // Передаём Scanner в конструктор
    }

    // Метод для получения экземпляра ProjectScanner
    public ProjectScanner getProjectScanner() {
        LOGGER.debug("Creating ProjectScanner (FileScannerImpl)");
        return new FileScannerImpl();
    }

    // Метод для получения экземпляра FilterConfiguration (из ресурса)
    public FilterConfiguration getFilterConfiguration() {
        LOGGER.debug("Loading FilterConfiguration from resource");
        try {
            return JsonFilterConfiguration.loadFromJsonResource("/filter-config.json");
        } catch (Exception e) {
            LOGGER.error("Failed to load FilterConfiguration: {}", e.getMessage(), e);
            // В реальном приложении нужно корректно обработать ошибку загрузки конфигурации
            throw new RuntimeException("Cannot start without FilterConfiguration", e);
        }
    }

    // Метод для получения экземпляра FileFilter
    public FileFilter getFileFilter() {
        LOGGER.debug("Creating FileFilter (FileFilterImpl)");
        FilterConfiguration filterConfig = getFilterConfiguration(); // Получаем конфигурацию
        return new FileFilterImpl(filterConfig);
    }

    // Метод для получения экземпляра ProjectAnalyzer
    public ProjectAnalyzer getProjectAnalyzer() {
        LOGGER.debug("Creating ProjectAnalyzer (DefaultProjectAnalyzer)");
        ProjectScanner scanner = getProjectScanner(); // Получаем scanner
        FileFilter filter = getFileFilter(); // Получаем filter
        return new DefaultProjectAnalyzer(scanner, filter); // Передаём зависимости в конструктор
    }

    // Метод для получения экземпляра ReportGenerator
    public ReportGenerator getReportGenerator() {
        LOGGER.debug("Creating ReportGenerator (ReportGeneratorImpl)");
        return new ReportGeneratorImpl(); // ReportGeneratorImpl может не зависеть от других бинов напрямую
    }

    // Метод для получения экземпляра AnalysisConfig от пользователя
    public com.example.auditor.model.AnalysisConfig getUserConfig() {
        UserInterface ui = getUserInterface(); // Получаем UI (с уже внедрённым Scanner)
        return ui.getUserConfig(); // Запрашиваем у UI конфигурацию
    }
}