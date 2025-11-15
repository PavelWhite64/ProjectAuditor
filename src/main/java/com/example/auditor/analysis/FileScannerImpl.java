// src/main/java/com/example/auditor/analysis/FileScannerImpl.java
package com.example.auditor.analysis;

import com.example.auditor.core.ProjectScanner;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.ProgressBar;
import com.example.auditor.utils.FileTypeClassifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FileScannerImpl implements ProjectScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerImpl.class);

    // Статическое поле для хранения игнорируемых директорий
    private static volatile Set<String> ignoredDirectories = null;
    private static final Object lock = new Object(); // Объект для синхронизации

    @Override
    public List<FileInfo> scan(Path projectPath) throws IOException {
        List<FileInfo> files = new ArrayList<>();

        // Прогресс-бар инициализируется, но обновляется только при фактическом добавлении файлов
        ProgressBar progressBar = new ProgressBar("Сканирование файлов", 100); // Временно 100 или 0, т.к. точное кол-во неизвестно
        AtomicInteger processed = new AtomicInteger(0); // Используем AtomicInteger

        // Используем SimpleFileVisitor для обхода дерева файлов
        Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Получаем имя директории
                String dirName = dir.getFileName().toString();

                // Проверяем, нужно ли игнорировать эту директорию
                // Получаем список игнорируемых директорий (загружается при первом обращении)
                Set<String> ignoredDirs = getIgnoredDirectories();
                if (ignoredDirs.contains(dirName)) {
                    LOGGER.debug("Пропуск подкаталога: {}", dir);
                    return FileVisitResult.SKIP_SUBTREE; // Пропускаем всю поддиректорию
                }

                // Обрабатываем директорию (например, для прогресс-бара, если нужно)
                // progressBar.update(processed.incrementAndGet()); // Не обновляем прогресс для директорий
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                if (Files.isRegularFile(filePath)) {
                    try {
                        // --- ОТЛАДКА ---
                        String relativePath = projectPath.relativize(filePath).toString().replace('\\', '/');
                        LOGGER.debug("Scanning file - Full: {}, Relative: {}", filePath, relativePath);

                        FileInfo fileInfo = new FileInfo(
                                filePath,
                                filePath.getFileName().toString(),
                                relativePath,
                                attrs.size(),
                                getFileExtension(filePath.getFileName().toString()),
                                FileTypeClassifier.classify(filePath.getFileName().toString()) // Используем классификатор
                        );
                        files.add(fileInfo);
                    } catch (Exception e) { // Ловим Exception, включая IOException от FileTypeClassifier
                        LOGGER.error("Ошибка при обработке файла {}: {}", filePath, e.getMessage(), e); // Логируем ошибку с трейсом
                        // Продолжаем сканирование остальных файлов
                    } finally {
                        // Обновляем прогресс-бар В ЛЮБОМ СЛУЧАЕ после попытки обработать файл
                        progressBar.update(processed.incrementAndGet()); // Используем метод incrementAndGet()
                    }
                } else {
                    // Если файл не regular (например, символическая ссылка, FIFO и т.д.)
                    // всё равно обновляем прогресс, так как он был учтён в Files.walk().count()
                    // НЕТ, в данном случае мы не знаем общее количество файлов, так как Files.walk не используется.
                    // Прогресс-бар обновляется только при добавлении регулярных файлов.
                    // progressBar.update(processed.incrementAndGet());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                // Игнорируем ошибки доступа к отдельным файлам/каталогам
                LOGGER.warn("Ошибка доступа к файлу/каталогу: {} ({})", file, exc.getMessage()); // Используем warn для предупреждений
                // Прогресс-бар обновляется только при обработке регулярных файлов
                // progressBar.update(processed.incrementAndGet());
                return FileVisitResult.CONTINUE;
            }
        });

        progressBar.finish();
        return files;
    }

    // Приватный метод для получения инициализированного списка (с одиночной загрузкой)
    private static Set<String> getIgnoredDirectories() {
        if (ignoredDirectories == null) {
            synchronized (lock) {
                if (ignoredDirectories == null) {
                    ignoredDirectories = loadIgnoredDirectoriesFromResource("/scanner-config.json");
                }
            }
        }
        return ignoredDirectories;
    }

    // Приватный метод для загрузки списка из JSON-ресурса
    private static Set<String> loadIgnoredDirectoriesFromResource(String resourcePath) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream resourceStream = FileScannerImpl.class.getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                throw new IOException("Не найден ресурс: " + resourcePath);
            }
            JsonNode rootNode = mapper.readTree(resourceStream);
            JsonNode ignoredDirsNode = rootNode.get("ignoredDirectories");

            if (ignoredDirsNode == null || !ignoredDirsNode.isArray()) {
                LOGGER.error("Неверный формат JSON в ресурсе {}: отсутствует массив 'ignoredDirectories'", resourcePath);
                return Collections.emptySet(); // Возвращаем пустой сет в случае ошибки
            }

            // --- ИСПРАВЛЕНИЕ: Используем цикл for-each ---
            java.util.Set<String> set = new java.util.HashSet<>();
            for (JsonNode node : ignoredDirsNode) {
                // Убедимся, что элемент массива - строка
                if (node != null && node.isValueNode()) {
                    set.add(node.asText());
                } else {
                    LOGGER.warn("Найден нестроковый элемент в массиве 'ignoredDirectories': {}. Пропущен.", node);
                }
            }
            // --- /ИСПРАВЛЕНИЕ ---

            LOGGER.debug("Загружено {} игнорируемых директорий из {}", set.size(), resourcePath);
            return set;

        } catch (IOException e) {
            LOGGER.error("Ошибка при загрузке игнорируемых директорий из {}: {}", resourcePath, e.getMessage(), e);
            return Collections.emptySet(); // Возвращаем пустой сет в случае ошибки
        }
    }


    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1); // Без точки
        }
        return null; // или " "
    }
}