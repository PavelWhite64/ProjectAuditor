// src/main/java/com/example/auditor/Main.java
package com.example.auditor;

import com.example.auditor.analysis.DefaultProjectAnalyzer;
import com.example.auditor.analysis.FileFilterImpl;
import com.example.auditor.analysis.FileScannerImpl;
import com.example.auditor.core.ProjectAnalyzer;
import com.example.auditor.core.ProjectScanner;
import com.example.auditor.core.FileFilter;
import com.example.auditor.core.UserInterface;
import com.example.auditor.core.ReportGenerator;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.reporting.ReportGeneratorImpl;
import com.example.auditor.ui.InteractivePrompter;
import com.example.auditor.utils.ConsoleColors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Главный класс приложения ProjectAuditor.
 * Собирает компоненты и запускает цикл анализа.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(ConsoleColors.CYAN + "🚀 Запуск Project Auditor v1.0..." + ConsoleColors.RESET);

        try {
            // 1. Создаем зависимости (Dependency Injection "вручную")
            UserInterface ui = new InteractivePrompter(); // Взаимодействие с пользователем
            ProjectScanner scanner = new FileScannerImpl(); // Сканирование файлов
            FileFilter fileFilter = new FileFilterImpl(); // Фильтрация файлов
            ProjectAnalyzer analyzer = new DefaultProjectAnalyzer(scanner, fileFilter); // Анализ проекта
            ReportGenerator generator = new ReportGeneratorImpl(); // Генерация отчетов

            // 2. Получаем конфигурацию от пользователя
            System.out.println("Получение настроек анализа...");
            AnalysisConfig config = ui.getUserConfig();

            // 3. Выполняем анализ
            System.out.println("\nНачало анализа проекта...");
            AnalysisResult result = analyzer.analyze(config);

            // 4. Генерируем отчеты
            System.out.println("\nГенерация отчетов...");
            Path outputDir = config.getProjectPath().getParent().resolve("auditor_output"); // Папка рядом с проектом
            generator.generate(result, config, outputDir);

            System.out.println(ConsoleColors.GREEN + "\n🎉 АНАЛИЗ ЗАВЕРШЕН УСПЕШНО!" + ConsoleColors.RESET);

        } catch (Exception e) {
            System.err.println(ConsoleColors.RED + "❌ Произошла ошибка: " + e.getMessage() + ConsoleColors.RESET);
            e.printStackTrace();
        }
    }
}