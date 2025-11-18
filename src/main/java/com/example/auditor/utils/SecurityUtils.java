package com.example.auditor.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Утилиты безопасности для проверки путей файлов */
public class SecurityUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtils.class);

  private SecurityUtils() {
    // Утилитный класс - приватный конструктор
  }

  /** Проверяет, что путь находится внутри базовой директории и не является симлинком */
  public static boolean isSafePath(Path filePath, Path baseDirectory) {
    try {
      // Проверяем, что файл находится внутри базовой директории
      if (!isPathInsideBaseDirectory(filePath, baseDirectory)) {
        LOGGER.warn("Попытка доступа к файлу вне базовой директории: {}", filePath);
        return false;
      }

      // Проверяем, что это не символическая ссылка
      if (Files.isSymbolicLink(filePath)) {
        LOGGER.warn("Обнаружена символическая ссылка: {}", filePath);
        return false;
      }

      return true;

    } catch (Exception e) {
      LOGGER.error("Ошибка проверки безопасности пути {}: {}", filePath, e.getMessage());
      return false;
    }
  }

  /** Проверяет, находится ли путь внутри базового каталога */
  public static boolean isPathInsideBaseDirectory(Path filePath, Path baseDirectoryPath) {
    try {
      Path normalizedFilePath = filePath.normalize().toAbsolutePath();
      Path normalizedBasePath = baseDirectoryPath.normalize().toAbsolutePath();

      Path relativePath = normalizedBasePath.relativize(normalizedFilePath);
      return !relativePath.toString().startsWith("..");

    } catch (IllegalArgumentException e) {
      LOGGER.warn(
          "Не удалось определить относительный путь: {} -> {}", baseDirectoryPath, filePath);
      return false;
    }
  }

  /** Проверяет директорию на безопасность */
  public static boolean isSafeDirectory(Path dirPath, Path baseDirectory) {
    try {
      // Проверяем, что директория находится внутри базовой
      if (!isPathInsideBaseDirectory(dirPath, baseDirectory)) {
        LOGGER.warn("Попытка доступа к директории вне базовой: {}", dirPath);
        return false;
      }

      // Проверяем, что это не символическая ссылка
      if (Files.isSymbolicLink(dirPath)) {
        LOGGER.warn("Обнаружена символическая ссылка-директория: {}", dirPath);
        return false;
      }

      return true;

    } catch (Exception e) {
      LOGGER.error("Ошибка проверки безопасности директории {}: {}", dirPath, e.getMessage());
      return false;
    }
  }
}
