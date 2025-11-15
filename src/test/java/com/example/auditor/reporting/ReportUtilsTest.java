package com.example.auditor.reporting;

import com.example.auditor.ProjectAuditorTest;
import com.example.auditor.core.FileIconService;
import com.example.auditor.core.FileSystem;
import com.example.auditor.core.DefaultFileSystem;
import com.example.auditor.model.FileInfo;
import com.example.auditor.utils.DefaultFileIconService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportUtilsTest extends ProjectAuditorTest {

    private final FileIconService fileIconService = new DefaultFileIconService();
    private final FileSystem fileSystem = new DefaultFileSystem();

    @Test
    void shouldEscapeMarkdownCorrectly() {
        // Given
        String input = "Hello *world* _italic_ `code` [link] # header";

        // When
        String result = ReportUtils.escapeMarkdown(input);

        // Then
        assertThat(result).isEqualTo("Hello \\*world\\* \\_italic\\_ \\`code\\` \\[link\\] \\# header");
    }

    @Test
    void shouldEscapeHtmlCorrectly() {
        // Given
        String input = "Hello <world> & \"friends\" 'test'";

        // When
        String result = ReportUtils.escapeHtml(input);

        // Then
        assertThat(result).isEqualTo("Hello &lt;world&gt; &amp; &quot;friends&quot; &#x27;test&#x27;");
    }

    @Test
    void shouldEscapeJsonCorrectly() {
        // Given
        String input = "Hello \"world\" \n \t \r \\ backslash";

        // When
        String result = ReportUtils.escapeJson(input);

        // Then
        assertThat(result).isEqualTo("Hello \\\"world\\\" \\n \\t \\r \\\\ backslash");
    }

    @Test
    void shouldReadFileContentWithinLimits() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        String content = "Line 1\nLine 2\nLine 3\nLine 4\nLine 5";
        Files.writeString(testFile, content);

        // When
        String result = ReportUtils.readFileContent(testFile, tempDir, 1000, 3, fileSystem);

        // Then
        assertThat(result).contains("Line 1", "Line 2", "Line 3");
        assertThat(result).contains("CONTENT TRUNCATED");
        assertThat(result).doesNotContain("Line 4", "Line 5");
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        // Given
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);

        // When
        String result = ReportUtils.readFileContent(emptyFile, tempDir, 1000, 10, fileSystem);

        // Then
        assertThat(result).isEqualTo("<!-- EMPTY FILE -->");
    }

    @Test
    void shouldHandleFileOutsideBaseDirectory() throws IOException {
        // Given
        Path outsideFile = tempDir.resolve("outside.txt");
        Files.writeString(outsideFile, "secret content");

        Path differentBase = tempDir.resolve("other");
        Files.createDirectories(differentBase);

        // When
        String result = ReportUtils.readFileContent(outsideFile, differentBase, 1000, 10, fileSystem);

        // Then
        assertThat(result).isEqualTo("<!-- SECURITY: File outside base directory -->");
    }

    @Test
    void shouldGenerateTreeMarkdownCorrectly() throws IOException {
        // Given
        Path projectDir = createTestProjectStructure();
        List<FileInfo> files = List.of(
                new FileInfo(projectDir.resolve("src/main/java/com/example/Main.java"),
                        "Main.java", "src/main/java/com/example/Main.java", 100, "java", "CODE"),
                new FileInfo(projectDir.resolve("src/main/java/com/example/Utils.java"),
                        "Utils.java", "src/main/java/com/example/Utils.java", 150, "java", "CODE"),
                new FileInfo(projectDir.resolve("README.md"),
                        "README.md", "README.md", 50, "md", "DOC")
        );

        // When
        String tree = ReportUtils.generateTreeMarkdown(files, fileIconService);

        // Then
        assertThat(tree).contains("src", "main", "java", "com", "example", "Main.java", "Utils.java", "README.md");
        assertThat(tree).contains("[JAVA]", "[DOC]");
    }

    @Test
    void shouldHandleLargeFileGracefully() throws IOException {
        // Given
        // Создаем файл с меньшим количеством строк, чтобы он точно был меньше лимита размера
        Path largeFile = createLargeTestFile(tempDir, "huge.txt", 30); // 30 строк вместо 100
        long fileSize = Files.size(largeFile);

        // Убедимся, что файл меньше лимита размера (увеличим лимит до 2000 байт)
        // When - устанавливаем лимит строк 20 (меньше чем 30 строк в файле)
        String result = ReportUtils.readFileContent(largeFile, tempDir, 2000, 20, fileSystem);

        // Then - должен быть сообщение о truncation
        assertThat(result).contains("CONTENT TRUNCATED");
        assertThat(result).contains("Test line 19"); // Последняя прочитанная строка
        assertThat(result).doesNotContain("Test line 20"); // Следующая строка не должна быть прочитана
    }

    @Test
    void shouldHandleVeryLargeFile() throws IOException {
        // Given - создаем действительно большой файл
        Path veryLargeFile = createLargeTestFile(tempDir, "very_huge.txt", 10000);
        long fileSize = Files.size(veryLargeFile);

        // When - устанавливаем очень маленький лимит размера
        String result = ReportUtils.readFileContent(veryLargeFile, tempDir, 100, 100, fileSystem);

        // Then - должен быть сообщение о том, что файл слишком большой
        assertThat(result).contains("FILE TOO LARGE");
        assertThat(result).contains(String.valueOf(fileSize));
    }

    @Test
    void shouldHandleFileExactlyAtSizeLimit() throws IOException {
        // Given - создаем файл, который точно на границе лимита
        Path exactSizeFile = createLargeTestFile(tempDir, "exact.txt", 10);
        long fileSize = Files.size(exactSizeFile);

        // When - устанавливаем лимит размера равным размеру файла
        String result = ReportUtils.readFileContent(exactSizeFile, tempDir, fileSize, 100, fileSystem);

        // Then - файл должен быть прочитан полностью без сообщения о превышении размера
        assertThat(result).doesNotContain("FILE TOO LARGE");
        assertThat(result).contains("Test line 9"); // Последняя строка
    }

    @Test
    void shouldReturnCurrentDate() {
        // When
        String date = ReportUtils.getCurrentDate();

        // Then
        assertThat(date).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }
}