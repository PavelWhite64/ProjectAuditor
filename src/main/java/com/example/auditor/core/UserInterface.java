package com.example.auditor.core;

import com.example.auditor.model.AnalysisConfig;

/**
 * Интерфейс для компонента взаимодействия с пользователем.
 */
public interface UserInterface {
    /**
     * Запрашивает у пользователя все необходимые настройки анализа.
     *
     * @return Объект AnalysisConfig с заполненными параметрами.
     */
    AnalysisConfig getUserConfig();
}