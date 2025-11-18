package com.example.auditor.model;

import java.util.List;

/** Класс для хранения результата анализа проекта. */
public class AnalysisResult {
  private final List<FileInfo> fileInfoList; // Теперь используем наш FileInfo из model
  private final String projectName;
  private final String projectType; // Определенный тип проекта (Java, Go, etc.)
  private final long totalSizeKB;
  private final int totalFiles;

  // Конструктор
  public AnalysisResult(
      List<FileInfo> fileInfoList,
      String projectName,
      String projectType,
      long totalSizeKB,
      int totalFiles) {
    this.fileInfoList = fileInfoList;
    this.projectName = projectName;
    this.projectType = projectType;
    this.totalSizeKB = totalSizeKB;
    this.totalFiles = totalFiles;
  }

  // Геттеры
  public List<FileInfo> getFileInfoList() {
    return fileInfoList;
  } // Теперь возвращает List<FileInfo> из model

  public String getProjectName() {
    return projectName;
  }

  public String getProjectType() {
    return projectType;
  }

  public long getTotalSizeKB() {
    return totalSizeKB;
  }

  public int getTotalFiles() {
    return totalFiles;
  }
}
