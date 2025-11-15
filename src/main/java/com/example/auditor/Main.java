package com.example.auditor;

import com.example.auditor.config.DefaultComponentFactory;
import com.example.auditor.runner.ApplicationRunner;
import com.example.auditor.service.UserConfigService;

/**
 * Главный класс приложения ProjectAuditor.
 * Теперь только точка входа, вся логика вынесена в ApplicationRunner.
 */
public class Main {

    public static void main(String[] args) {
        // Создаем фабрику компонентов
        DefaultComponentFactory componentFactory = new DefaultComponentFactory();

        // Создаем сервис пользовательской конфигурации
        UserConfigService userConfigService = new UserConfigService(
                componentFactory.createUserInterface()
        );

        // Создаем и запускаем приложение
        ApplicationRunner runner = new ApplicationRunner(componentFactory, userConfigService);
        runner.run();
    }
}