package com.example.auditor.utils;

import com.example.auditor.ProjectAuditorTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest extends ProjectAuditorTest {

    @Test
    void shouldAllowPathsInsideBaseDirectory() throws IOException {
        // Given
        Path baseDir = tempDir;
        Path safeFile = tempDir.resolve("src/Main.java");
        Files.createDirectories(safeFile.getParent());
        Files.createFile(safeFile);

        // When & Then
        assertThat(SecurityUtils.isSafePath(safeFile, baseDir)).isTrue();
        assertThat(SecurityUtils.isSafeDirectory(safeFile.getParent(), baseDir)).isTrue();
    }

    @Test
    void shouldRejectPathsOutsideBaseDirectory() throws IOException {
        // Given
        Path baseDir = tempDir.resolve("project");
        Files.createDirectories(baseDir);

        Path outsideFile = tempDir.resolve("outside.txt");
        Files.createFile(outsideFile);

        // When & Then
        assertThat(SecurityUtils.isSafePath(outsideFile, baseDir)).isFalse();
        assertThat(SecurityUtils.isPathInsideBaseDirectory(outsideFile, baseDir)).isFalse();
    }

    @Test
    void shouldHandleRelativePaths() {
        // Given
        Path baseDir = tempDir;
        Path relativePath = Path.of("src/main/java");

        // When
        boolean result = SecurityUtils.isPathInsideBaseDirectory(
                baseDir.resolve(relativePath), baseDir);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleComplexPaths() {
        // Given
        Path baseDir = tempDir;
        Path complexPath = baseDir.resolve("src/../src/main/./java/../../src");

        // When
        boolean result = SecurityUtils.isPathInsideBaseDirectory(complexPath, baseDir);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectPathTraversal() {
        // Given
        Path baseDir = tempDir;
        Path traversalPath = baseDir.resolve("../../etc/passwd");

        // When
        boolean result = SecurityUtils.isPathInsideBaseDirectory(traversalPath.normalize(), baseDir);

        // Then
        assertThat(result).isFalse();
    }
}