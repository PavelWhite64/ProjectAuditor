package com.example.auditor.runner;

import com.example.auditor.config.ComponentFactory;
import com.example.auditor.core.ProjectAnalyzer;
import com.example.auditor.core.ReportGenerator;
import com.example.auditor.core.UserInterface;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import com.example.auditor.service.UserConfigService;
import com.example.auditor.utils.ConsoleColors;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Основной класс для запуска и координации работы приложения. Теперь корректно управляет ресурсами
 * UserInterface.
 */
public class ApplicationRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRunner.class);

  private final ComponentFactory componentFactory;
  private final UserConfigService userConfigService;

  public ApplicationRunner(ComponentFactory componentFactory, UserConfigService userConfigService) {
    this.componentFactory = componentFactory;
    this.userConfigService = userConfigService;
  }

  public void run() {
    System.out.println(
        ConsoleColors.CYAN + "🚀 Запуск Project Auditor v1.0... " + ConsoleColors.RESET);

    // Используем try-with-resources для гарантированного закрытия UserInterface
    try (UserInterface userInterface = componentFactory.createUserInterface()) {
      // 1. Получаем конфигурацию от пользователя
      System.out.println("Получение настроек анализа... ");
      AnalysisConfig userConfig = userConfigService.getUserConfig();

      // 2. Создаем анализатор и выполняем анализ
      System.out.println("\nНачало анализа проекта... ");
      ProjectAnalyzer analyzer = componentFactory.createProjectAnalyzer();
      AnalysisResult result = analyzer.analyze(userConfig);

      // 3. Генерируем отчеты
      System.out.println("\nГенерация отчетов... ");
      ReportGenerator generator = componentFactory.createReportGenerator();
      Path outputDir = userConfig.getProjectPath().getParent().resolve("auditor_output");
      generator.generate(result, userConfig, outputDir);

      System.out.println(
          ConsoleColors.GREEN + "\n🎉 АНАЛИЗ ЗАВЕРШЕН УСПЕШНО! " + ConsoleColors.RESET);

    } catch (Exception e) {
      LOGGER.error("Произошла ошибка: {}", e.getMessage(), e);
      System.out.println(
          ConsoleColors.RED
              + "\n❌ Анализ завершен с ошибкой. Подробности в логах. "
              + ConsoleColors.RESET);
    }
  }
}
