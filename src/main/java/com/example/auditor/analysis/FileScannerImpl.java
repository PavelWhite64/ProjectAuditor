package com.example.auditor.analysis;

import com.example.auditor.core.FileTypeClassifier;
import com.example.auditor.core.ProjectScanner;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.ConsoleProgressIndicator;
import com.example.auditor.utils.FileExtensionUtils;
import com.example.auditor.utils.SecurityUtils;
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

    private final FileTypeClassifier fileTypeClassifier;

    public FileScannerImpl(FileTypeClassifier fileTypeClassifier) {
        this.fileTypeClassifier = fileTypeClassifier;
    }

    @Override
    public List<FileInfo> scan(Path projectPath) throws IOException {
        List<FileInfo> files = new ArrayList<>();

        // Прогресс-бар инициализируется, но обновляется только при фактическом добавлении файлов
        ConsoleProgressIndicator progressBar = new ConsoleProgressIndicator("Сканирование файлов", 100);
        AtomicInteger processed = new AtomicInteger(0);

        // Используем SimpleFileVisitor для обхода дерева файлов
        Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Проверяем безопасность директории
                if (!SecurityUtils.isSafeDirectory(dir, projectPath)) {
                    LOGGER.warn("Пропуск небезопасной директории: {}", dir);
                    return FileVisitResult.SKIP_SUBTREE;
                }

                // Получаем имя директории
                String dirName = dir.getFileName().toString();

                // Проверяем, нужно ли игнорировать эту директорию
                Set<String> ignoredDirs = getIgnoredDirectories();
                if (ignoredDirs.contains(dirName)) {
                    LOGGER.debug("Пропуск игнорируемого каталога: {}", dir);
                    return FileVisitResult.SKIP_SUBTREE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                // Проверяем безопасность файла
                if (!SecurityUtils.isSafePath(filePath, projectPath)) {
                    LOGGER.warn("Пропуск небезопасного файла: {}", filePath);
                    return FileVisitResult.CONTINUE;
                }

                if (Files.isRegularFile(filePath)) {
                    try {
                        String relativePath = projectPath.relativize(filePath).toString().replace('\\', '/');
                        LOGGER.debug("Scanning file - Full: {}, Relative: {}", filePath, relativePath);

                        FileInfo fileInfo = new FileInfo(
                                filePath,
                                filePath.getFileName().toString(),
                                relativePath,
                                attrs.size(),
                                FileExtensionUtils.getExtension(filePath.getFileName().toString(),
                                        FileExtensionUtils.ExtensionFormat.WITHOUT_DOT),
                                fileTypeClassifier.classify(filePath.getFileName().toString()) // Используем инжектированный классификатор
                        );
                        files.add(fileInfo);
                    } catch (Exception e) {
                        LOGGER.error("Ошибка при обработке файла {}: {}", filePath, e.getMessage(), e);
                    } finally {
                        progressBar.update(processed.incrementAndGet());
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                LOGGER.warn("Ошибка доступа к файлу/каталогу: {} ({})", file, exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });

        progressBar.finish();
        LOGGER.info("Сканирование завершено. Найдено {} файлов.", files.size());
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
                return Collections.emptySet();
            }

            java.util.Set<String> set = new java.util.HashSet<>();
            for (JsonNode node : ignoredDirsNode) {
                if (node != null && node.isValueNode()) {
                    set.add(node.asText());
                } else {
                    LOGGER.warn("Найден нестроковый элемент в массиве 'ignoredDirectories': {}. Пропущен.", node);
                }
            }

            LOGGER.debug("Загружено {} игнорируемых директорий из {}", set.size(), resourcePath);
            return set;

        } catch (IOException e) {
            LOGGER.error("Ошибка при загрузке игнорируемых директорий из {}: {}", resourcePath, e.getMessage(), e);
            return Collections.emptySet();
        }
    }
}