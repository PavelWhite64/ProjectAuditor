package com.example.auditor.core;

import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.AnalysisResult;
import java.nio.file.Path;

/**
 * Стратегия генерации отчетов. Соответствует принципу Open-Closed - можно добавлять новые форматы
 * без изменения существующего кода.
 */
public interface ReportStrategy {

  /** Поддерживает ли стратегия указанный формат вывода */
  boolean supports(AnalysisConfig.OutputFormat format);

  /** Генерирует отчет */
  void generateReport(
      AnalysisResult result, AnalysisConfig config, Path outputDir, String outputFileName);

  /** Возвращает расширение файла для данного формата */
  String getFileExtension();

  /** Возвращает описание формата */
  String getFormatDescription();
}
