package com.example.auditor.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Интерфейс для абстракции файловой системы.
 * Позволяет мокать файловые операции в тестах и обеспечивает лучшую тестируемость.
 */
public interface FileSystem {

    /**
     * Читает содержимое файла как строку с указанной кодировкой.
     */
    String readFileContent(Path path) throws IOException;

    /**
     * Возвращает размер файла в байтах.
     */
    long getFileSize(Path path) throws IOException;

    /**
     * Проверяет, существует ли файл или директория.
     */
    boolean exists(Path path);

    /**
     * Проверяет, является ли путь директорией.
     */
    boolean isDirectory(Path path);

    /**
     * Проверяет, является ли путь обычным файлом.
     */
    boolean isRegularFile(Path path);

    /**
     * Возвращает список всех файлов в директории (нерекурсивно).
     */
    List<Path> listFiles(Path dir) throws IOException;

    /**
     * Рекурсивно обходит дерево файлов.
     */
    void walkFileTree(Path start, FileVisitor visitor) throws IOException;

    /**
     * Посетитель для обхода дерева файлов.
     */
    interface FileVisitor {
        FileVisitResult preVisitDirectory(Path dir) throws IOException;
        FileVisitResult visitFile(Path file) throws IOException;
        FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException;
        FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException;
    }

    /**
     * Результат посещения файла/директории.
     */
    enum FileVisitResult {
        CONTINUE,
        TERMINATE,
        SKIP_SUBTREE,
        SKIP_SIBLINGS
    }
}