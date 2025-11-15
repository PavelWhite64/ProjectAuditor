// src/main/java/com/example/auditor/analysis/config/JsonFilterConfiguration.java
package com.example.auditor.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Реализация FilterConfiguration, загружающая списки из JSON-ресурса.
 */
public class JsonFilterConfiguration implements FilterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonFilterConfiguration.class);

    private final List<String> includePatterns;
    private final List<String> excludePatterns;
    private final Set<String> blacklistedExtensions;

    private JsonFilterConfiguration(List<String> includePatterns, List<String> excludePatterns, Set<String> blacklistedExtensions) {
        this.includePatterns = Collections.unmodifiableList(includePatterns);
        this.excludePatterns = Collections.unmodifiableList(excludePatterns);
        this.blacklistedExtensions = Collections.unmodifiableSet(blacklistedExtensions);
    }

    @Override
    public List<String> getIncludePatterns() {
        return includePatterns;
    }

    @Override
    public List<String> getExcludePatterns() {
        return excludePatterns;
    }

    @Override
    public Set<String> getBlacklistedExtensions() {
        return blacklistedExtensions;
    }

    /**
     * Загружает конфигурацию из JSON-ресурса в classpath.
     *
     * @param resourcePath Путь к ресурсу (например, "/filter-config.json").
     * @return Экземпляр JsonFilterConfiguration.
     * @throws IOException Если не удалось загрузить или распарсить файл.
     */
    public static JsonFilterConfiguration loadFromJsonResource(String resourcePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream resourceStream = JsonFilterConfiguration.class.getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            throw new IOException("Не найден ресурс: " + resourcePath);
        }
        JsonNode rootNode = mapper.readTree(resourceStream);

        List<String> includePatterns = mapper.convertValue(rootNode.get("includePatterns"), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        List<String> excludePatterns = mapper.convertValue(rootNode.get("excludePatterns"), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        Set<String> blacklistedExtensions = mapper.convertValue(rootNode.get("blacklistedExtensions"), mapper.getTypeFactory().constructCollectionType(Set.class, String.class));

        // --- ОТЛАДКА ---
        LOGGER.debug("Loaded includePatterns (first 3):");
        includePatterns.stream().limit(3).forEach(pattern -> LOGGER.debug("  '{}' (Length: {})", pattern, pattern.length()));

        LOGGER.debug("Loaded excludePatterns (first 5):");
        excludePatterns.stream().limit(5).forEach(pattern -> LOGGER.debug("  '{}' (Length: {})", pattern, pattern.length()));

        LOGGER.debug("Loaded blacklistedExtensions (first 10):");
        blacklistedExtensions.stream().limit(10).forEach(ext -> LOGGER.debug("  '{}' (Length: {})", ext, ext.length()));
        // --- /ОТЛАДКА ---

        return new JsonFilterConfiguration(includePatterns, excludePatterns, blacklistedExtensions);
    }
}