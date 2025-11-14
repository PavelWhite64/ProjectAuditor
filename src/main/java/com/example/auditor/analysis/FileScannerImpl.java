// src/main/java/com/example/auditor/analysis/FileScannerImpl.java
package com.example.auditor.analysis;

import com.example.auditor.core.ProjectScanner;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.ProgressBar; // Убедитесь, что ProgressBar находится тут или импорт верен

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Реализация ProjectScanner, использующая Files.walkFileTree для сканирования.
 */
public class FileScannerImpl implements ProjectScanner {

    @Override
    public List<FileInfo> scan(Path projectPath) {
        List<FileInfo> allFiles = new ArrayList<>();
        // Предварительный проход для подсчета файлов для прогресс-бара
        // Это может быть неточно, если файлы добавляются/удаляются во время сканирования,
        // но даст приблизительный прогресс.
        long totalEstimatedFiles = 0;
        try {
            totalEstimatedFiles = (int) Files.walk(projectPath)
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            System.err.println("Ошибка при подсчете файлов для прогресс-бара: " + e.getMessage());
            // Продолжаем со значением 1, чтобы прогресс-бар не сломался
            totalEstimatedFiles = 1;
        }

        ProgressBar progressBar = new ProgressBar("Сканирование файлов", (int) totalEstimatedFiles);
        AtomicInteger processedCount = new AtomicInteger(0); // Используем AtomicInteger

        try {
            Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file)) {
                        String relativePath = projectPath.relativize(file).toString().replace('\\', '/');
                        String name = file.getFileName().toString();
                        long length = attrs.size();
                        String extension = getExtension(name);
                        // Тип файла пока устанавливаем как "FILE", логика определения типа (DATA, SCRIPT и т.д.)
                        // может быть вынесена в отдельный компонент или добавлена позже в фильтре.
                        String type = "FILE";

                        allFiles.add(new FileInfo(file, name, relativePath, length, extension, type));
                        progressBar.update(processedCount.incrementAndGet()); // Увеличиваем и обновляем прогресс
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            progressBar.finish();
        } catch (IOException e) {
            System.err.println("Ошибка при сканировании проекта: " + e.getMessage());
            e.printStackTrace();
            // В реальном приложении лучше выбросить исключение или вернуть пустой список с логом
        }

        return allFiles;
    }

    // Вспомогательный метод для получения расширения
    private String getExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return null; // или возвращать пустую строку ""
    }
}