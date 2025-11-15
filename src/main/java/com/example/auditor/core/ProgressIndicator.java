package com.example.auditor.core;

/**
 * Интерфейс для индикаторов прогресса выполнения операций.
 */
public interface ProgressIndicator {

    /**
     * Обновляет прогресс выполнения
     * @param currentStep текущий шаг выполнения
     */
    void update(int currentStep);

    /**
     * Завершает отображение прогресса
     */
    void finish();

    /**
     * Устанавливает общее количество шагов
     * @param totalSteps общее количество шагов
     */
    void setTotalSteps(int totalSteps);
}