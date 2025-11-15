package com.example.auditor.model;

import java.nio.file.Path;

/**
 * Класс, представляющий информацию о файле проекта.
 */
public class FileInfo {
    private final Path fullName; // Полный путь к файлу
    private final String name; // Имя файла
    private final String relativePath; // Относительный путь от корня проекта
    private final long length; // Размер файла в байтах
    private final String extension; // Расширение файла (без точки)
    private final String type; // Тип файла (FILE, DATA, SCRIPT, DOC, etc.)
    private boolean priority; // Приоритетный ли файл (соответствует include-паттернам)

    public FileInfo(Path fullName, String name, String relativePath, long length, String extension, String type) {
        // Вызов конструктора с приоритетом по умолчанию false
        this(fullName, name, relativePath, length, extension, type, false);
    }

    // Добавлен конструктор с параметром priority
    public FileInfo(Path fullName, String name, String relativePath, long length, String extension, String type, boolean priority) {
        this.fullName = fullName;
        this.name = name;
        this.relativePath = relativePath;
        this.length = length;
        this.extension = extension != null ? extension : "";
        this.type = type != null ? type : "FILE"; // Значение по умолчанию
        this.priority = priority; // Установка приоритета
    }

    // Геттеры
    public Path getFullName() {
        return fullName;
    }

    public String getName() {
        return name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public long getLength() {
        return length;
    }

    public String getExtension() {
        return extension;
    }

    public String getType() {
        return type;
    }

    // Геттер и сеттер для приоритета
    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }
}