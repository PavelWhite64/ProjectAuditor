package com.example.auditor.service;

import com.example.auditor.core.UserInterface;
import com.example.auditor.model.AnalysisConfig;

/**
 * Сервис для получения конфигурации анализа от пользователя.
 * Теперь работает с AutoCloseable UserInterface.
 */
public class UserConfigService {
    private final UserInterface userInterface;

    public UserConfigService(UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public AnalysisConfig getUserConfig() {
        return userInterface.getUserConfig();
    }
}