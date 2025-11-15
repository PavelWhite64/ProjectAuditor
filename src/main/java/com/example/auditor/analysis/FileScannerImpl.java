// src/main/java/com/example/auditor/analysis/FileScannerImpl.java
package com.example.auditor.analysis;

import com.example.auditor.core.ProjectScanner;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.ProgressBar;
import com.example.auditor.utils.FileTypeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FileScannerImpl implements ProjectScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerImpl.class);

    @Override
    public List<FileInfo> scan(Path projectPath) throws IOException {
        List<FileInfo> files = new ArrayList<>();

        // Подсчитываем количество файлов для прогресс-бара
        long totalFiles = 0;
        try {
            totalFiles = Files.walk(projectPath)
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            LOGGER.error("Ошибка при подсчете файлов: {}", e.getMessage(), e); // Логируем ошибку с трейсом
            // В реальном коде логируйте правильно
            return files; // Возвращаем пустой список или обрабатываем ошибку иначе
        }

        ProgressBar progressBar = new ProgressBar("Сканирование файлов", (int) totalFiles);
        AtomicInteger processed = new AtomicInteger(0); // Используем AtomicInteger

        // Используем SimpleFileVisitor для обхода дерева файлов
        Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                if (Files.isRegularFile(filePath)) { // Исправлено: filePath -> filePath
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
                    progressBar.update(processed.incrementAndGet());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                // Игнорируем ошибки доступа к отдельным файлам/каталогам
                LOGGER.warn("Ошибка доступа к файлу/каталогу: {} ({})", file, exc.getMessage()); // Используем warn для предупреждений
                // Обновляем прогресс, даже если доступ к файлу не удался
                progressBar.update(processed.incrementAndGet());
                return FileVisitResult.CONTINUE;
            }
        });

        progressBar.finish();
        return files;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1); // Без точки
        }
        return null; // или " "
    }
}