package com.example.auditor;

import com.example.auditor.config.ApplicationConfig;
import com.example.auditor.core.ProjectAnalyzer;
import com.example.auditor.core.ReportGenerator;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.utils.ConsoleColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Главный класс приложения ProjectAuditor.
 * Собирает компоненты и запускает цикл анализа.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println(ConsoleColors.CYAN + "🚀 Запуск Project Auditor v1.0... " + ConsoleColors.RESET); // Это UI-вывод в консоль, можно оставить

        try {
            // 1. Создаем конфигурацию (Dependency Injection Container)
            ApplicationConfig config = new ApplicationConfig();

            // 2. Получаем зависимости из конфигурации
            ProjectAnalyzer analyzer = config.getProjectAnalyzer(); // Внедрение зависимости
            ReportGenerator generator = config.getReportGenerator(); // Внедрение зависимости

            // 3. Получаем конфигурацию от пользователя
            System.out.println("Получение настроек анализа... ");
            AnalysisConfig userConfig = config.getUserConfig(); // Внедрение зависимости UI и вызов метода

            // 4. Выполняем анализ
            System.out.println("\nНачало анализа проекта... ");
            AnalysisResult result = analyzer.analyze(userConfig);

            // 5. Генерируем отчеты
            System.out.println("\nГенерация отчетов... ");
            Path outputDir = userConfig.getProjectPath().getParent().resolve("auditor_output"); // Папка рядом с проектом
            generator.generate(result, userConfig, outputDir);

            System.out.println(ConsoleColors.GREEN + "\n🎉 АНАЛИЗ ЗАВЕРШЕН УСПЕШНО! " + ConsoleColors.RESET); // UI-вывод

        } catch (Exception e) {
            LOGGER.error("Произошла ошибка: {}", e.getMessage(), e); // Логируем ошибку с трейсом
        }
    }
}