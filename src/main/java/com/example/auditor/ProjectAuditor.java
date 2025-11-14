package com.example.auditor;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import com.example.auditor.utils.ConsoleColors;

import java.util.concurrent.Callable;

@Command(name = "project-auditor",
        mixinStandardHelpOptions = true,
        version = "Project Auditor 1.0",
        description = "Interactive tool for preparing projects for LLM audit")
public class ProjectAuditor implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        // === UTF-8 FIX FOR WINDOWS ===
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                java.util.List<String> commands = new java.util.ArrayList<>();
                commands.add("cmd.exe");
                commands.add("/c");
                commands.add("chcp 65001 > nul"); // 65001 = UTF-8 code page

                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.start().waitFor();
            } catch (Exception e) {
                System.err.println("Failed to change console code page");
            }
        }

        System.out.println(ConsoleColors.CYAN + "================================================================");
        System.out.println(ConsoleColors.GREEN + "           PROJECT AUDITOR v1.0 - INTERACTIVE MODE");
        System.out.println(ConsoleColors.CYAN + "================================================================" + ConsoleColors.RESET);

        InteractivePrompter prompter = new InteractivePrompter();
        FileScanner fileScanner = new FileScanner();
        ReportGenerator reportGenerator = new ReportGenerator();
        ProjectAnalyzer projectAnalyzer = new ProjectAnalyzer();

        // STEP 1: PROJECT SELECTION
        System.out.println(ConsoleColors.CYAN + "\nSTEP 1: PROJECT SELECTION" + ConsoleColors.RESET);
        String currentDir = System.getProperty("user.dir");
        String projectPath = prompter.readValidatedPath(
                "Enter the path to the project to analyze",
                currentDir
        );

        // Path validation
        if (!fileScanner.isValidProjectPath(projectPath)) {
            System.err.println(ConsoleColors.RED + "ERROR: Specified path does not exist or is not a directory!" + ConsoleColors.RESET);
            prompter.close();
            return 1;
        }

        String projectName = fileScanner.getProjectName(projectPath);
        String projectSummary = fileScanner.getProjectSummary(projectPath);

        System.out.println(ConsoleColors.GREEN + "✓ Project: " + projectName + ConsoleColors.RESET);
        System.out.println(ConsoleColors.BLUE + "✓ " + projectSummary + ConsoleColors.RESET);

        // STEP 2: OUTPUT CONFIGURATION
        System.out.println(ConsoleColors.CYAN + "\nSTEP 2: OUTPUT CONFIGURATION" + ConsoleColors.RESET);
        int outputChoice = prompter.showMenu(
                "Select output format:",
                new String[] {
                        "Markdown (.md) - for LLM analysis",
                        "HTML (.html) - for web viewing",
                        "Both formats",
                        "Structure only (without file contents)"
                },
                0
        );

        boolean lightMode = false;
        boolean generateMarkdown = true;
        boolean generateHtml = false;
        String outputExt = "md";

        switch (outputChoice) {
            case 1:
                generateMarkdown = true;
                generateHtml = false;
                outputExt = "md";
                break;
            case 2:
                generateMarkdown = false;
                generateHtml = true;
                outputExt = "html";
                break;
            case 3:
                generateMarkdown = true;
                generateHtml = true;
                outputExt = "md+html";
                break;
            case 4:
                generateMarkdown = true;
                lightMode = true;
                outputExt = "md (light)";
                break;
        }

        String defaultOutput = "project-audit";
        String outputName = prompter.readString(
                "Output file name (without extension)",
                defaultOutput
        );

        String markdownFile = generateMarkdown ? outputName + ".md" : null;
        String htmlFile = generateHtml ? outputName + ".html" : null;

        // STEP 3: FILTERING SETTINGS
        System.out.println(ConsoleColors.CYAN + "\nSTEP 3: FILTERING SETTINGS" + ConsoleColors.RESET);
        boolean includeLarge = prompter.readYesNo(
                "Include large files (>500KB)?",
                false
        );

        int maxSizeKB = includeLarge ? 5000 : 500;
        if (includeLarge) {
            maxSizeKB = prompter.readNumber(
                    "Maximum file size (KB)",
                    5000,
                    100,
                    50000
            );
        }

        boolean useGitignore = prompter.readYesNo(
                "Use .gitignore rules for file exclusion?",
                true
        );

        // STEP 4: ADDITIONAL OPTIONS
        System.out.println(ConsoleColors.CYAN + "\nSTEP 4: ADDITIONAL OPTIONS" + ConsoleColors.RESET);
        boolean generateJson = prompter.readYesNo(
                "Create JSON file with metadata?",
                false
        );

        String jsonFile = generateJson ? outputName + ".json" : null;

        boolean openAfterComplete = prompter.readYesNo(
                "Open results after completion?",
                true
        );

        // STEP 5: SETTINGS CONFIRMATION
        System.out.println(ConsoleColors.CYAN + "\nSTEP 5: SETTINGS CONFIRMATION" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.YELLOW + "The following settings will be applied:" + ConsoleColors.RESET);
        System.out.println("  • Project: " + projectName);
        System.out.println("  • Path: " + projectPath);
        System.out.println("  • Output formats: " + outputExt);
        if (markdownFile != null) System.out.println("  • Markdown: " + markdownFile);
        if (htmlFile != null) System.out.println("  • HTML: " + htmlFile);
        if (jsonFile != null) System.out.println("  • JSON: " + jsonFile);
        System.out.println("  • Mode: " + (lightMode ? "Light" : "Full"));
        System.out.println("  • Max file size: " + maxSizeKB + " KB");
        System.out.println("  • Use .gitignore: " + (useGitignore ? "Yes" : "No"));

        boolean confirm = prompter.readYesNo(
                "\nStart project analysis?",
                true
        );

        if (!confirm) {
            System.out.println(ConsoleColors.YELLOW + "Analysis canceled by user." + ConsoleColors.RESET);
            prompter.close();
            return 0;
        }

        // =========================================================================
        // MAIN ANALYSIS LOGIC
        // =========================================================================
        System.out.println("\n" + ConsoleColors.GREEN + "STARTING PROJECT ANALYSIS..." + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "================================================================" + ConsoleColors.RESET);

        // File search
        System.out.println(ConsoleColors.CYAN + "Searching for files..." + ConsoleColors.RESET);
        long startTime = System.currentTimeMillis();
        java.util.List<FileScanner.FileInfo> allFiles = fileScanner.getAllFiles(projectPath);
        long searchTime = System.currentTimeMillis() - startTime;

        System.out.println(ConsoleColors.BLUE +
                "Found " + allFiles.size() + " files in " + (searchTime/1000.0) + " sec" +
                ConsoleColors.RESET);

        // File filtering
        System.out.println(ConsoleColors.CYAN + "Filtering files..." + ConsoleColors.RESET);
        java.util.List<FileScanner.FileInfo> files = fileScanner.filterFiles(
                allFiles,
                projectPath,
                maxSizeKB,
                includeLarge,
                useGitignore
        );

        if (files.isEmpty()) {
            System.err.println(ConsoleColors.RED + "No suitable files found after filtering" + ConsoleColors.RESET);
            prompter.close();
            return 1;
        }

        System.out.println(ConsoleColors.GREEN + "Processed " + files.size() + " files for audit" + ConsoleColors.RESET);

        // Project analysis
        System.out.println(ConsoleColors.CYAN + "Analyzing project..." + ConsoleColors.RESET);
        String projectType = projectAnalyzer.getProjectType(projectPath, files);

        // Language statistics
        java.util.Map<String, Integer> languageStats = projectAnalyzer.getLanguageStats(files);
        java.util.Map<String, Integer> sizeStats = projectAnalyzer.getSizeStats(files);

        // Report generation
        if (generateMarkdown) {
            System.out.println(ConsoleColors.CYAN + "Generating Markdown report..." + ConsoleColors.RESET);
            reportGenerator.generateMarkdownReport(
                    files,
                    projectPath,
                    projectName,
                    projectType,
                    lightMode,
                    markdownFile
            );
            System.out.println(ConsoleColors.GREEN + "✓ Markdown report created: " + markdownFile + ConsoleColors.RESET);
        }

        if (generateHtml) {
            System.out.println(ConsoleColors.CYAN + "Generating HTML report..." + ConsoleColors.RESET);
            reportGenerator.generateHtmlReport(
                    files,
                    projectPath,
                    projectName,
                    projectType,
                    lightMode,
                    htmlFile
            );
            System.out.println(ConsoleColors.GREEN + "✓ HTML report created: " + htmlFile + ConsoleColors.RESET);
        }

        if (generateJson) {
            System.out.println(ConsoleColors.CYAN + "Generating JSON report..." + ConsoleColors.RESET);
            reportGenerator.generateJsonReport(
                    files,
                    projectPath,
                    projectName,
                    projectType,
                    jsonFile
            );
            System.out.println(ConsoleColors.GREEN + "✓ JSON report created: " + jsonFile + ConsoleColors.RESET);
        }

        // COMPLETION
        System.out.println("\n" + ConsoleColors.GREEN + "================================================================");
        System.out.println("🎉 ANALYSIS COMPLETED SUCCESSFULLY!");
        System.out.println("================================================================" + ConsoleColors.RESET);

        int totalFiles = files.size();
        int totalSizeKB = files.stream().mapToInt(f -> (int)(f.length / 1024)).sum();

        System.out.println("\nCreated files:");
        if (markdownFile != null) {
            System.out.println("  📄 Markdown: " + markdownFile);
            java.io.File mdFile = new java.io.File(markdownFile);
            if (mdFile.exists()) {
                double mdSize = mdFile.length() / 1024.0;
                System.out.println("     Size: " + String.format("%.2f", mdSize) + " KB");
            }
        }
        if (htmlFile != null) {
            System.out.println("  🌐 HTML: " + htmlFile);
            java.io.File htmlFileObj = new java.io.File(htmlFile);
            if (htmlFileObj.exists()) {
                double htmlSize = htmlFileObj.length() / 1024.0;
                System.out.println("     Size: " + String.format("%.2f", htmlSize) + " KB");
            }
        }
        if (jsonFile != null) {
            System.out.println("  📊 JSON: " + jsonFile);
            java.io.File jsonFileObj = new java.io.File(jsonFile);
            if (jsonFileObj.exists()) {
                double jsonSize = jsonFileObj.length() / 1024.0;
                System.out.println("     Size: " + String.format("%.2f", jsonSize) + " KB");
            }
        }

        System.out.println("\nProject statistics:");
        System.out.println("  📁 Files processed: " + totalFiles);
        System.out.println("  💾 Total size: " + totalSizeKB + " KB");
        System.out.println("  🏷️ Project type: " + projectType);
        System.out.println("  ⚡ Mode: " + (lightMode ? "Light" : "Full"));

        // Open results
        if (openAfterComplete) {
            System.out.println("");
            boolean openNow = prompter.readYesNo("Open results now?", true);
            if (openNow) {
                if (markdownFile != null && new java.io.File(markdownFile).exists()) {
                    try {
                        java.awt.Desktop.getDesktop().open(new java.io.File(markdownFile));
                        System.out.println(ConsoleColors.GREEN + "✓ Markdown file opened" + ConsoleColors.RESET);
                    } catch (Exception e) {
                        System.out.println(ConsoleColors.YELLOW + "Failed to open Markdown file" + ConsoleColors.RESET);
                    }
                }
                if (htmlFile != null && new java.io.File(htmlFile).exists()) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.io.File(htmlFile).toURI());
                        System.out.println(ConsoleColors.GREEN + "✓ HTML file opened in browser" + ConsoleColors.RESET);
                    } catch (Exception e) {
                        System.out.println(ConsoleColors.YELLOW + "Failed to open HTML file" + ConsoleColors.RESET);
                    }
                }
            }
        }

        System.out.println("\n" + ConsoleColors.GREEN + "Thank you for using Project Auditor! 🚀");
        System.out.println(ConsoleColors.CYAN + "================================================================" + ConsoleColors.RESET);

        prompter.close();
        return 0;
    }

    public static void main(String[] args) {
        // Force UTF-8 for Windows
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.stdout.encoding", "UTF-8");
            System.setProperty("sun.stderr.encoding", "UTF-8");
        }

        int exitCode = new CommandLine(new ProjectAuditor()).execute(args);
        System.exit(exitCode);
    }
}