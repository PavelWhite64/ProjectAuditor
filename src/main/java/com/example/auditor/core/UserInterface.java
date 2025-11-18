package com.example.auditor.core;

import com.example.auditor.model.AnalysisConfig;

/**
 * Интерфейс для компонента взаимодействия с пользователем. Теперь поддерживает AutoCloseable для
 * управления ресурсами.
 */
public interface UserInterface extends AutoCloseable {
  /**
   * Запрашивает у пользователя все необходимые настройки анализа.
   *
   * @return Объект AnalysisConfig с заполненными параметрами.
   */
  AnalysisConfig getUserConfig();

  /** Закрывает ресурсы, связанные с пользовательским интерфейсом. */
  @Override
  void close();
}
