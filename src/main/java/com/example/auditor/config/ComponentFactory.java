package com.example.auditor.config;

import com.example.auditor.core.*;

/**
 * Фабрика для создания компонентов приложения.
 * Разделяет ответственность создания и конфигурации бинов.
 */
public interface ComponentFactory {

    ProjectScanner createProjectScanner();

    FileFilter createFileFilter();

    ProjectAnalyzer createProjectAnalyzer();

    ReportGenerator createReportGenerator();

    UserInterface createUserInterface();
}