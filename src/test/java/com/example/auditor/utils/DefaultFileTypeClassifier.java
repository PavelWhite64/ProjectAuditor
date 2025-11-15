package com.example.auditor.utils;

import com.example.auditor.ProjectAuditorTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultFileTypeClassifierTest extends ProjectAuditorTest {

    private final DefaultFileTypeClassifier classifier = new DefaultFileTypeClassifier();

    @Test
    void shouldClassifyJavaFilesAsCode() {
        assertThat(classifier.classify("Main.java")).isEqualTo("CODE");
        assertThat(classifier.classify("Utils.kt")).isEqualTo("CODE");
        assertThat(classifier.classify("Script.scala")).isEqualTo("CODE");
    }

    @Test
    void shouldClassifyScriptFiles() {
        assertThat(classifier.classify("build.sh")).isEqualTo("SCRIPT");
        assertThat(classifier.classify("deploy.ps1")).isEqualTo("SCRIPT");
        assertThat(classifier.classify("setup.bat")).isEqualTo("SCRIPT");
    }

    @Test
    void shouldClassifyConfigFiles() {
        assertThat(classifier.classify("config.yml")).isEqualTo("CONFIG");
        assertThat(classifier.classify("settings.json")).isEqualTo("CONFIG");
        assertThat(classifier.classify("pom.xml")).isEqualTo("CONFIG");
    }

    @Test
    void shouldClassifyDocumentation() {
        assertThat(classifier.classify("README.md")).isEqualTo("DOC");
        assertThat(classifier.classify("CHANGELOG.txt")).isEqualTo("DOC");
        assertThat(classifier.classify("docs.rst")).isEqualTo("DOC");
    }

    @Test
    void shouldClassifyBinaryFiles() {
        assertThat(classifier.classify("image.png")).isEqualTo("BINARY");
        assertThat(classifier.classify("library.jar")).isEqualTo("BINARY");
        assertThat(classifier.classify("data.zip")).isEqualTo("BINARY");
    }

    @ParameterizedTest
    @CsvSource({
            "Dockerfile, CONFIG",
            "docker-compose.yml, CONFIG",
            "build.gradle, CONFIG",
            "package.json, CONFIG",
            "go.mod, CONFIG",
            "unknown.xyz, FILE",
            "file-without-extension, FILE",
            "'.hiddenfile', FILE"
    })
    void shouldClassifyVariousFiles(String filename, String expectedType) {
        assertThat(classifier.classify(filename)).isEqualTo(expectedType);
    }

    @Test
    void shouldHandleNullFileName() {
        assertThat(classifier.classify(null)).isEqualTo("FILE");
    }

    @Test
    void shouldHandleEmptyFileName() {
        assertThat(classifier.classify("")).isEqualTo("FILE");
    }
}