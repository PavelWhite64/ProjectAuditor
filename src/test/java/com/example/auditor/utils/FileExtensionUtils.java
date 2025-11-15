package com.example.auditor.utils;

import com.example.auditor.ProjectAuditorTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FileExtensionUtilsTest extends ProjectAuditorTest {

    @ParameterizedTest
    @CsvSource({
            "Main.java, java, .java, java",
            "config.YML, yml, .yml, yml",
            "README.md, md, .md, md",
            "file.with.dots.txt, txt, .txt, txt",
            "noextension, '', '', ''",
            ".hidden, '', '', ''",
            "null, '', '', ''"
    })
    void shouldExtractExtensionsCorrectly(String filename, String withoutDot, String withDot, String lowercase) {
        // Handle null filename specially
        if ("null".equals(filename)) {
            filename = null;
        }

        assertThat(FileExtensionUtils.getExtension(filename, FileExtensionUtils.ExtensionFormat.WITHOUT_DOT))
                .isEqualTo(withoutDot);
        assertThat(FileExtensionUtils.getExtension(filename, FileExtensionUtils.ExtensionFormat.WITH_DOT))
                .isEqualTo(withDot);
        assertThat(FileExtensionUtils.getExtension(filename, FileExtensionUtils.ExtensionFormat.LOWERCASE))
                .isEqualTo(lowercase);
    }

    @Test
    void shouldCheckExtensionPresence() {
        assertThat(FileExtensionUtils.hasExtension("Main.java", "java")).isTrue();
        assertThat(FileExtensionUtils.hasExtension("Main.java", ".java")).isTrue();
        assertThat(FileExtensionUtils.hasExtension("Script.py", "java")).isFalse();
        assertThat(FileExtensionUtils.hasExtension("noextension", "txt")).isFalse();
    }

    @Test
    void shouldCheckAnyExtension() {
        Set<String> extensions = Set.of("java", "py", "js");

        assertThat(FileExtensionUtils.hasAnyExtension("Main.java", extensions)).isTrue();
        assertThat(FileExtensionUtils.hasAnyExtension("script.py", extensions)).isTrue();
        assertThat(FileExtensionUtils.hasAnyExtension("app.js", extensions)).isTrue();
        assertThat(FileExtensionUtils.hasAnyExtension("data.txt", extensions)).isFalse();
        assertThat(FileExtensionUtils.hasAnyExtension("noextension", extensions)).isFalse();
    }

    @Test
    void shouldHandleNullInputs() {
        assertThat(FileExtensionUtils.getExtension(null, FileExtensionUtils.ExtensionFormat.WITHOUT_DOT))
                .isEqualTo("");
        assertThat(FileExtensionUtils.hasExtension(null, "java")).isFalse();
        assertThat(FileExtensionUtils.hasAnyExtension(null, Set.of("java"))).isFalse();
        assertThat(FileExtensionUtils.hasAnyExtension("Main.java", null)).isFalse();
    }

    @Test
    void shouldHandleEmptyFileName() {
        assertThat(FileExtensionUtils.getExtension("", FileExtensionUtils.ExtensionFormat.WITHOUT_DOT))
                .isEqualTo("");
        assertThat(FileExtensionUtils.getExtension("", FileExtensionUtils.ExtensionFormat.WITH_DOT))
                .isEqualTo("");
        assertThat(FileExtensionUtils.getExtension("", FileExtensionUtils.ExtensionFormat.LOWERCASE))
                .isEqualTo("");
    }
}