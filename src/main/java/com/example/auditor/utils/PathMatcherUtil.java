// src/main/java/com/example/auditor/utils/PathMatcherUtil.java
package com.example.auditor.utils;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Утилита для сопоставления путей с glob-паттернами. */
public class PathMatcherUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathMatcherUtil.class);

  //    /**
  //     * Проверяет, соответствует ли filePath хотя бы одному из glob-паттернов.
  //     *
  //     * @param filePath Относительный путь к файлу (например, ".git/objects/pack/pack-abc.pack").
  //     *                 Должен использовать '/' как разделитель.
  //     * @param patterns Список glob-паттернов (например, ["**/.git/**", "**/*.jar"]).
  //            * @return true, если файл соответствует хотя бы одному паттерну, иначе false.
  //            */
  public static boolean matchFile(String filePath, List<String> patterns) {
    // Нормализуем путь к файлу (заменяем \ на /)
    String normalizedPath = filePath.replace('\\', '/');

    // Создаем java.nio.file.Path из нормализованного пути.
    // Используем Paths.get("").resolve(...).normalize() для создания относительного пути,
    // чтобы избежать проблем с абсолютными путями внутри Paths.get().
    java.nio.file.Path path = java.nio.file.Paths.get("").resolve(normalizedPath).normalize();

    for (String pattern : patterns) {
      // Нормализуем паттерн (заменяем \ на /)
      String normalizedPattern = pattern.replace('\\', '/');
      try {
        // Создаем PathMatcher для конкретного паттерна
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + normalizedPattern);

        // Проверяем совпадение
        if (matcher.matches(path)) {
          // LOGGER.debug("MATCH FOUND - Path: '{}' matches Pattern: '{}'", normalizedPath,
          // normalizedPattern);
          return true; // Совпадение найдено
        }
      } catch (Exception e) {
        LOGGER.error(
            "ERROR in PathMatcher for pattern '{}': {}",
            normalizedPattern,
            e.getMessage(),
            e); // Логируем с трейсом
        // В реальном коде не нужно крашить приложение из-за ошибки паттерна.
        // Лучше логировать и продолжить.
        continue;
      }
    }
    return false; // Совпадений не найдено
  }
}
