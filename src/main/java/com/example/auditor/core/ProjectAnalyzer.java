package com.example.auditor.core;

import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;

/**
 * Интерфейс для компонента анализа проекта.
 */
public interface ProjectAnalyzer {
    /**
     * Выполняет анализ проекта на основе переданной конфигурации.
     *
     * @param config Объект AnalysisConfig с настройками.
     * @return Объект AnalysisResult с результатами анализа.
     */
    AnalysisResult analyze(AnalysisConfig config);
}