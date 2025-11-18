package com.example.auditor.core;

import com.example.auditor.model.FileInfo;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Интерфейс для компонента сканирования файлов проекта. */
public interface ProjectScanner {
  /**
   * Сканирует проект по указанному пути.
   *
   * @param projectPath Путь к корню проекта.
   * @return Список FileInfo, представляющих файлы проекта.
   */
  List<FileInfo> scan(Path projectPath) throws IOException;
}
