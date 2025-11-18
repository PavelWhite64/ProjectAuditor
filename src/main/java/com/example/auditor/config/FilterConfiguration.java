package com.example.auditor.config;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Интерфейс для предоставления конфигурации фильтрации файлов. */
public interface FilterConfiguration {

  /**
   * Возвращает список паттернов для приоритетных файлов. Файлы, соответствующие этим паттернам,
   * считаются приоритетными, но не исключаются, если не соответствуют им.
   *
   * @return Список glob-паттернов.
   */
  List<String> getIncludePatterns();

  /**
   * Возвращает список паттернов для жёстко исключаемых файлов. Файлы, соответствующие этим
   * паттернам, будут исключены из анализа.
   *
   * @return Список glob-паттернов.
   */
  List<String> getExcludePatterns();

  /**
   * Возвращает список расширений файлов, которые нужно исключить. Эти расширения исключаются ДО
   * проверки сложных паттернов exclude. Расширения должны быть в нижнем регистре и начинаться с
   * точки (например, ".jar").
   *
   * @return Множество расширений файлов.
   */
  Set<String> getBlacklistedExtensions();

  /**
   * Загружает конфигурацию из JSON-ресурса.
   *
   * @param resourcePath Путь к ресурсу (например, "/filter-config.json").
   * @return Экземпляр FilterConfiguration.
   * @throws IOException Если не удалось загрузить или распарсить файл.
   */
  static FilterConfiguration fromJsonResource(String resourcePath) throws IOException {
    return JsonFilterConfiguration.loadFromJsonResource(resourcePath);
  }
}
