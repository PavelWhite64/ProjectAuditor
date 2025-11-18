package com.example.auditor.core;

/** Сервис для получения иконок и информации о файлах */
public interface FileIconService {

  /** Возвращает иконку для терминала на основе имени файла */
  String getIcon(String fileName);

  /** Возвращает иконку для HTML на основе имени файла */
  String getHtmlIcon(String fileName);

  /** Возвращает язык программирования для подсветки синтаксиса */
  String getLanguage(String fileName);
}
