package com.example.auditor;

import com.example.auditor.utils.PathMatcher;
import com.example.auditor.utils.ProgressBar;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class FileScanner {

    public static class FileInfo {
        public String name;
        public String fullName;
        public long length;
        public String relativePath;

        public FileInfo(String name, String fullName, long length, String relativePath) {
            this.name = name;
            this.fullName = fullName;
            this.length = length;
            this.relativePath = relativePath;
        }
    }

    public boolean isValidProjectPath(String path) {
        java.io.File file = new java.io.File(path);
        return file.exists() && file.isDirectory();
    }

    public String getProjectName(String path) {
        java.io.File file = new java.io.File(path);
        return file.getName();
    }

    public String getProjectSummary(String path) {
        try {
            Path projectPath = Paths.get(path);
            List<Path> allItems = new ArrayList<>();
            List<Path> files = new ArrayList<>();

            Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    allItems.add(file);
                    files.add(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    allItems.add(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            long totalSize = files.stream()
                    .mapToLong(f -> {
                        try {
                            return Files.size(f);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();

            double sizeMB = totalSize / (1024.0 * 1024.0);
            return String.format("Элементов: %d, Файлов: %d, Размер: %.2fMB",
                    allItems.size(), files.size(), sizeMB);

        } catch (IOException e) {
            return "Ошибка анализа";
        }
    }

    public List<FileInfo> getAllFiles(String projectPath) {
        Path path = Paths.get(projectPath);
        List<FileInfo> files = new ArrayList<>();

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fullName = file.toAbsolutePath().toString();
                    String name = file.getFileName().toString();
                    long length = attrs.size();
                    String relativePath = path.relativize(file).toString().replace("\\", "/");

                    files.add(new FileInfo(name, fullName, length, relativePath));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Ошибка при поиске файлов: " + e.getMessage());
        }

        return files;
    }

    private List<String> getIncludePatterns() {
        return Arrays.asList(
                "*.java", "*.kt", "*.scala",
                "*.js", "*.ts", "*.jsx", "*.tsx", "*.vue", "*.svelte",
                "*.py", "*.pyx", "*.pxd", "*.ipynb",
                "*.go", "go.mod", "go.sum",
                "*.rs", "Cargo.toml", "Cargo.lock",
                "*.cpp", "*.c", "*.h", "*.hpp", "*.cc", "*.cxx",
                "*.cs", "*.vb", "*.fs", "*.fsx",
                "*.php", "*.phtml", "*.php4", "*.php5", "*.php7",
                "*.rb", "*.erb", "Gemfile", "Gemfile.lock",
                "*.swift", "*.m", "*.mm",
                "*.pl", "*.pm", "*.t",
                "*.lua", "*.r", "*.scala", "*.clj", "*.cljs",
                "*.yml", "*.yaml", "*.xml", "*.json", "*.jsonc", "*.json5",
                "*.toml", "*.ini", "*.cfg", "*.conf", "*.config",
                "pom.xml", "build.gradle", "settings.gradle", "build.gradle.kts",
                "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
                "requirements.txt", "Pipfile", "pyproject.toml", "poetry.lock",
                "composer.json", "composer.lock", "Makefile", "CMakeLists.txt",
                "*.props", "*.targets", "*.csproj", "*.vbproj", "*.fsproj", "*.sln",
                "*.md", "*.markdown", "*.txt", "*.rst", "*.adoc", "*.asciidoc",
                "*.sh", "*.bash", "*.zsh", "*.fish", "*.ps1", "*.psm1", "*.psd1",
                "*.bat", "*.cmd", "*.vbs", "*.wsf",
                "Dockerfile", "docker-compose*.yml", "docker-compose*.yaml",
                "Jenkinsfile", ".gitlab-ci.yml", ".github/workflows/*.yml",
                "*.sql", "*.psql", "*.plsql", "*.pgsql",
                "*.html", "*.htm", "*.xhtml", "*.css", "*.scss", "*.sass", "*.less",
                "*.ejs", "*.pug", "*.jade", "*.mustache", "*.handlebars", "*.hbs",
                ".gitignore", ".dockerignore", ".env.example", ".env.sample", ".env.template",
                "LICENSE", "COPYING", "README*", "CHANGELOG*", "AUTHORS*", "CONTRIBUTING*",
                ".editorconfig", ".prettierrc", ".eslintrc*", ".babelrc", "tsconfig.json",
                "webpack.config.*", "vite.config.*", "rollup.config.*", "jest.config.*"
        );
    }

    private List<String> getExcludePatterns() {
        return Arrays.asList(
                // Системные директории
                "**/.git/**", "**/.svn/**", "**/.hg/**", "**/.bzr/**",
                "**/.idea/**", "**/.vscode/**", "**/.vs/**",
                "**/.gradle/**", "**/.mvn/**", "**/.cache/**",

                // Артефакты сборки
                "**/build/**", "**/target/**", "**/out/**",
                "**/dist/**", "**/bin/**", "**/obj/**",
                "**/*.jar", "**/*.war", "**/*.ear", "**/*.class",

                // Отчеты тестов
                "**/test-results/**", "**/reports/tests/**",
                "**/surefire-reports/**", "**/failsafe-reports/**",

                // Сгенерированная документация
                "**/javadoc/**", "**/apidocs/**",

                // Временные и кэш-файлы
                "**/*.log", "**/*.tmp", "**/*.temp", "**/*.cache",

                // Файлы с секретами (КРИТИЧЕСКИ ВАЖНО!)
                "**/config.*", "**/*secret*", "**/*password*", "**/*credential*",
                "**/*.key", "**/*.pem", "**/*.crt", "**/*.cert", "**/*.pfx",
                "**/*.property", // ВКЛЮЧАЯ config.property!
                "**/application-*.yml", "**/application-*.yaml", "**/application-*.properties",
                "**/local.*", "**/dev.*", "**/prod.*", "**/test.*", "**/.env*",

                // IDE и редакторы
                "**/*.iml", "**/*.idea/**", "**/*.project", "**/*.classpath",

                // Бинарные файлы
                "**/*.png", "**/*.jpg", "**/*.jpeg", "**/*.gif", "**/*.ico", "**/*.svg",
                "**/*.pdf", "**/*.doc", "**/*.docx", "**/*.xls", "**/*.xlsx",
                "**/*.zip", "**/*.tar", "**/*.gz", "**/*.bz2", "**/*.7z"
        );
    }

    public List<FileInfo> filterFiles(
            List<FileInfo> allFiles,
            String projectPath,
            int maxSizeKB,
            boolean includeLarge,
            boolean useGitignore
    ) {
        List<String> includePatterns = getIncludePatterns();
        List<String> excludePatterns = getExcludePatterns();
        List<String> gitIgnorePatterns = new ArrayList<>();

        if (useGitignore) {
            gitIgnorePatterns = new GitIgnoreParser().parseGitIgnore(projectPath);
        }

        long maxSizeBytes = maxSizeKB * 1024L;

        List<FileInfo> filteredFiles = new ArrayList<>();
        ProgressBar progressBar = new ProgressBar("Фильтрация файлов", allFiles.size());

        int processed = 0;
        for (FileInfo file : allFiles) {
            progressBar.update(processed++);

            // Проверка include паттернов
            boolean includeMatch = PathMatcher.matchFile(file.relativePath, includePatterns);
            if (!includeMatch) continue;

            // Проверка exclude паттернов
            boolean excludeMatch = PathMatcher.matchFile(file.relativePath, excludePatterns);
            if (excludeMatch) continue;

            // Проверка .gitignore
            boolean gitIgnoreMatch = PathMatcher.matchFile(file.relativePath, gitIgnorePatterns);
            if (gitIgnoreMatch) continue;

            // Проверка размера
            if (!includeLarge && file.length > maxSizeBytes) continue;

            filteredFiles.add(file);
        }

        progressBar.finish();
        return filteredFiles;
    }
}