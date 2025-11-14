// src/main/java/com/example/auditor/core/ReportGenerator.java
package com.example.auditor.core;

import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;

import java.nio.file.Path;

/**
 * Интерфейс для компонента генерации отчетов.
 */
public interface ReportGenerator {
    /**
     * Генерирует отчеты на основе результатов анализа и конфигурации.
     * @param result Объект AnalysisResult с данными для отчета.
     * @param config Объект AnalysisConfig с настройками вывода.
     * @param outputDir Директория, куда сохранять отчеты.
     */
    void generate(AnalysisResult result, AnalysisConfig config, Path outputDir);
}