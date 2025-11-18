package com.example.auditor.config;


import com.example.auditor.core.FileFilter;
import com.example.auditor.core.ProjectAnalyzer;
import com.example.auditor.core.ProjectScanner;
import com.example.auditor.core.ReportGenerator;
import com.example.auditor.core.UserInterface;

/**
 * Фабрика для создания компонентов приложения. Разделяет ответственность создания и конфигурации
 * бинов.
 */
public interface ComponentFactory {

  ProjectScanner createProjectScanner();

  FileFilter createFileFilter();

  ProjectAnalyzer createProjectAnalyzer();

  ReportGenerator createReportGenerator();

  UserInterface createUserInterface();
}
