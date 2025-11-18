package com.example.auditor.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitIgnoreParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(GitIgnoreParser.class);

  public List<String> parseGitIgnore(String projectPath) {
    Path gitIgnorePath = Paths.get(projectPath, ".gitignore");
    if (!Files.exists(gitIgnorePath)) {
      LOGGER.debug("Файл .gitignore не найден в: {}", projectPath);
      return new ArrayList<>();
    }

    try {
      List<String> lines =
          Files.readAllLines(gitIgnorePath, java.nio.charset.StandardCharsets.UTF_8);
      List<String> patterns = new ArrayList<>();

      for (String line : lines) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue; // Пропускаем пустые строки и комментарии
        }

        // Обработка исключений (начинающихся с !)
        boolean isNegation = line.startsWith("!");
        if (isNegation) {
          line = line.substring(1); // Убираем !
        }

        // Конвертация .gitignore паттернов в glob
        String globPattern = convertToGlobPattern(line);

        // В текущей реализации мы игнорируем исключения (!),
        // так как PathMatcherUtil не поддерживает их напрямую.
        // В реальной системе нужно будет учитывать исключения при фильтрации.
        if (!isNegation) {
          patterns.add(globPattern);
        }
      }

      LOGGER.info("Загружено {} правил из .gitignore", patterns.size());
      return patterns;

    } catch (IOException e) {
      LOGGER.error("Не удалось обработать .gitignore: {}", e.getMessage(), e); // Логируем с трейсом
      return new ArrayList<>();
    }
  }

  private String convertToGlobPattern(String gitIgnorePattern) {
    // Убираем завершающий слеш для директорий (если он есть и не экранирован)
    boolean isDirectory = gitIgnorePattern.endsWith("/") && !gitIgnorePattern.endsWith("\\/");
    if (isDirectory) {
      gitIgnorePattern = gitIgnorePattern.substring(0, gitIgnorePattern.length() - 1);
    }

    // Обработка паттернов
    StringBuilder globBuilder = new StringBuilder();

    // Если начинается с /, это корневой паттерн
    boolean isRooted = gitIgnorePattern.startsWith("/");
    if (isRooted) {
      gitIgnorePattern = gitIgnorePattern.substring(1); // Убираем начальный /
      // Корневой паттерн: **/ + паттерн
      globBuilder.append("**/").append(gitIgnorePattern); // Не вызываем escapeGlobMetachars здесь
    } else {
      // Паттерн с путем или простой паттерн файла
      // Используем **/ для поиска в любых подкаталогах
      globBuilder.append("**/").append(gitIgnorePattern); // Не вызываем escapeGlobMetachars здесь
    }

    // Обработка ** (глубокий поиск), * (один уровень), ? (один символ)
    // ВАЖНО: Обрабатываем ** до обработки одиночных *, и учитываем контекст []
    String globPattern = globBuilder.toString();

    // --- УПРОЩЁННАЯ ЛОГИКА ОБРАБОТКИ **, *, ? (С УЧЁТОМ КОНТЕКСТА [] И ЭКРАНИРОВАНИЯ) ---
    // ВНИМАНИЕ: Используем * и ?, а НЕ [^/]* и [^/], чтобы избежать ошибки PathMatcher
    StringBuilder finalPattern = new StringBuilder();
    boolean inCharClass = false; // Флаг: находимся ли мы внутри []
    boolean escaped = false; // Флаг: предыдущий символ был \

    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);

      if (escaped) {
        // Предыдущий символ был \, текущий - экранированный
        finalPattern.append('\\').append(c); // Добавляем \ и текущий символ
        escaped = false; // Сбрасываем флаг
        continue;
      }

      if (c == '\\') {
        // Наткнулись на \, следующий символ будет экранирован
        escaped = true;
        continue; // Не добавляем \ сюда, он будет добавлен с экранированным символом
      }

      if (c == '[' && !inCharClass) {
        // Начало символьного класса
        inCharClass = true;
        finalPattern.append(c);
        continue;
      }

      if (c == ']' && inCharClass) {
        // Конец символьного класса
        inCharClass = false;
        finalPattern.append(c);
        continue;
      }

      // Теперь обрабатываем *, ?, **, только если НЕ внутри []
      if (!inCharClass) {
        if (c == '*' && i + 1 < globPattern.length() && globPattern.charAt(i + 1) == '*') {
          // Нашли ** (глубокий поиск)
          finalPattern.append("**"); // Оставляем ** как есть
          i++; // Пропускаем следующую *
        } else if (c == '*') {
          // Нашли одиночную * (один уровень, не внутри [])
          // ИСПОЛЬЗУЕМ * ВМЕСТО [^/]*, чтобы избежать ошибки PathMatcher
          finalPattern.append("*");
        } else if (c == '?') {
          // Нашли ? (один символ, не внутри [])
          // ИСПОЛЬЗУЕМ ? ВМЕСТО [^/], чтобы избежать ошибки PathMatcher
          finalPattern.append("?");
        } else {
          // Обычный символ вне []
          finalPattern.append(c);
        }
      } else {
        // Обычный символ внутри []
        finalPattern.append(c);
      }
    }

    // Если строка закончилась, а флаги остались включены - значит, синтаксис был неправильным
    // В реальной системе можно логировать ошибку.
    if (inCharClass) {
      LOGGER.warn("Незавершённый символьный класс в паттерне .gitignore: {}", globPattern);
    }
    if (escaped) {
      LOGGER.warn(
          "Незавершённая экранированная последовательность в паттерне .gitignore: {}", globPattern);
      // Восстанавливаем слеш, если он был в конце строки
      finalPattern.append('\\');
    }

    globPattern = finalPattern.toString();
    // --- /УПРОЩЁННАЯ ЛОГИКА ---

    // Добавляем звездочки для директорий (если был слеш в конце)
    if (isDirectory) {
      globPattern += "/**";
    }

    return globPattern;
  }

  // Метод escapeGlobMetachars больше не нужен, так как обработка происходит в основном цикле
  // с учётом контекста [].
}
