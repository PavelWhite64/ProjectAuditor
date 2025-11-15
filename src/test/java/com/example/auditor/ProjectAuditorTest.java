package com.example.auditor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Базовый класс для всех тестов ProjectAuditor.
 * Предоставляет общие утилиты и настройки.
 */
public abstract class ProjectAuditorTest {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProjectAuditorTest.class);

    @TempDir
    protected Path tempDir;

    @BeforeEach
    void setUp() {
        LOGGER.info("Running test in temporary directory: {}", tempDir);
    }

    /**
     * Создает тестовую файловую структуру для тестирования
     */
    protected Path createTestProjectStructure() throws IOException {
        // Создаем структуру проекта для тестирования
        Path srcDir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcDir);

        Path testDir = tempDir.resolve("src/test/java/com/example");
        Files.createDirectories(testDir);

        // Создаем несколько тестовых файлов
        Files.writeString(srcDir.resolve("Main.java"),
                "package com.example;\n\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World\");\n    }\n}");

        Files.writeString(srcDir.resolve("Utils.java"),
                "package com.example;\n\npublic class Utils {\n    public static void helper() {\n        // Test method\n    }\n}");

        Files.writeString(testDir.resolve("MainTest.java"),
                "package com.example;\n\nimport org.junit.jupiter.api.Test;\n\npublic class MainTest {\n    @Test\n    void testMain() {\n        // Test case\n    }\n}");

        // Конфигурационные файлы
        Files.writeString(tempDir.resolve("pom.xml"),
                "<?xml version=\"1.0\"?>\n<project>\n    <modelVersion>4.0.0</modelVersion>\n    <groupId>com.example</groupId>\n    <artifactId>test-project</artifactId>\n    <version>1.0.0</version>\n</project>");

        Files.writeString(tempDir.resolve("README.md"), "# Test Project\n\nThis is a test project for unit testing.");

        return tempDir;
    }

    /**
     * Создает большой тестовый файл для проверки ограничений
     */
    protected Path createLargeTestFile(Path directory, String filename, int lines) throws IOException {
        Path filePath = directory.resolve(filename);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            content.append("// Test line ").append(i).append("\n");
        }
        Files.writeString(filePath, content.toString());
        return filePath;
    }

    protected Path createVeryLargeTestFile(Path directory, String filename, int sizeInBytes) throws IOException {
        Path filePath = directory.resolve(filename);
        StringBuilder content = new StringBuilder();
        String line = "// This is a test line for large file content generation ";

        // Calculate how many lines we need to reach the desired size
        int bytesPerLine = line.length() + 1; // +1 for newline
        int linesNeeded = (int) Math.ceil((double) sizeInBytes / bytesPerLine);

        for (int i = 0; i < linesNeeded; i++) {
            content.append(line).append(i).append("\n");
        }

        Files.writeString(filePath, content.toString());
        return filePath;
    }
}