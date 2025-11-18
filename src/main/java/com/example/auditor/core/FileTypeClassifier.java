package com.example.auditor.core;

/** Интерфейс для классификации типов файлов */
public interface FileTypeClassifier {

  /**
   * Классифицирует файл на основе его имени
   *
   * @param fileName имя файла
   * @return тип файла в виде строки
   */
  String classify(String fileName);
}
