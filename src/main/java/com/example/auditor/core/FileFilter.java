package com.example.auditor.core;

import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.model.FileInfo;
import java.nio.file.Path;
import java.util.List;

/** Интерфейс для компонента фильтрации файлов проекта. */
public interface FileFilter {
  /**
   * Фильтрует список файлов на основе конфигурации.
   *
   * @param files Список файлов для фильтрации.
   * @param projectPath Путь к корню проекта (для .gitignore).
   * @param config Конфигурация анализа, содержащая критерии фильтрации.
   * @return Отфильтрованный список файлов.
   */
  List<FileInfo> filter(List<FileInfo> files, Path projectPath, AnalysisConfig config);
}
