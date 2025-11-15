package com.example.auditor.ui;

import com.example.auditor.core.UserInterface;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.utils.ConsoleColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of UserInterface, providing an interactive text interface
 * for obtaining analysis configuration from the user.
 * Uses BufferedReader with explicit UTF-8 encoding for proper Unicode support.
 */
public class InteractivePrompter implements UserInterface, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractivePrompter.class);

    private final BufferedReader reader;
    private boolean closed = false;

    // Конструктор принимает InputStream для гибкости (тесты, разные источники ввода)
    public InteractivePrompter(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    @Override
    public AnalysisConfig getUserConfig() {
        if (closed) {
            throw new IllegalStateException("InteractivePrompter is closed");
        }

        System.out.println(ConsoleColors.CYAN + "\n--- PROJECT AUDITOR SETUP ---" + ConsoleColors.RESET);

        // STEP 1: PROJECT SELECTION
        System.out.println(ConsoleColors.CYAN + "\nSTEP 1: PROJECT SELECTION " + ConsoleColors.RESET);
        String projectPathStr = readLine("Enter path to project [default: current directory]: ", ".");
        while (projectPathStr.isEmpty()) {
            projectPathStr = readLine("Path cannot be empty. Please re-enter: ", ".");
        }
        Path projectPath = Paths.get(projectPathStr).toAbsolutePath().normalize();
        System.out.println(" -> Selected project: " + projectPath);

        // STEP 2: OUTPUT CONFIGURATION
        System.out.println(ConsoleColors.CYAN + "\nSTEP 2: OUTPUT CONFIGURATION " + ConsoleColors.RESET);
        AnalysisConfig.OutputFormat outputFormat = getOutputFormat();
        String outputFileName = getOutputFileName();
        boolean generateJsonMetadata = readYesNo("Generate JSON metadata file? ", false);
        boolean openResultsAfterwards = readYesNo("Open results after completion? ", true);

        // STEP 3: FILTERING SETTINGS
        System.out.println(ConsoleColors.CYAN + "\nSTEP 3: FILTERING SETTINGS " + ConsoleColors.RESET);
        boolean useGitIgnore = readYesNo("Use .gitignore for filtering? ", true);
        long maxFileSizeKB = getMaxFileSizeKB();

        // STEP 4: ADDITIONAL OPTIONS
        System.out.println(ConsoleColors.CYAN + "\nSTEP 4: ADDITIONAL OPTIONS " + ConsoleColors.RESET);
        boolean lightMode = outputFormat == AnalysisConfig.OutputFormat.STRUCTURE_ONLY;
        if (outputFormat != AnalysisConfig.OutputFormat.STRUCTURE_ONLY) {
            lightMode = readYesNo("Use 'Structure Only' mode (ignores file contents)? ", false);
        }

        // STEP 5: SETTINGS CONFIRMATION
        System.out.println(ConsoleColors.CYAN + "\nSTEP 5: SETTINGS CONFIRMATION " + ConsoleColors.RESET);
        System.out.println(ConsoleColors.YELLOW + "The following settings will be applied: " + ConsoleColors.RESET);
        System.out.println(" • Project: " + projectPath);
        System.out.println(" • Output Format: " + outputFormat);
        System.out.println(" • Output File Name: " + outputFileName);
        System.out.println(" • JSON Metadata: " + (generateJsonMetadata ? "Yes" : "No"));
        System.out.println(" • Open After Completion: " + (openResultsAfterwards ? "Yes" : "No"));
        System.out.println(" • Use .gitignore: " + (useGitIgnore ? "Yes" : "No"));
        System.out.println(" • Max File Size: " + maxFileSizeKB + " KB");
        System.out.println(" • Mode: " + (lightMode ? "Light" : "Full"));

        boolean confirm = readYesNo("\nProceed with these settings? ", true);

        if (!confirm) {
            System.out.println(ConsoleColors.RED + "Settings rejected by user. Exiting. " + ConsoleColors.RESET);
            System.exit(0);
        }

        return new AnalysisConfig(
                projectPath,
                outputFormat,
                outputFileName,
                generateJsonMetadata,
                openResultsAfterwards,
                useGitIgnore,
                maxFileSizeKB,
                java.util.List.of(),
                lightMode
        );
    }

    // --- Helper methods for interaction ---
    private AnalysisConfig.OutputFormat getOutputFormat() {
        System.out.println("Select output format: ");
        System.out.println("[1] Markdown (.md) - for LLM analysis ");
        System.out.println("[2] HTML (.html) - for web viewing ");
        System.out.println("[3] Both formats ");
        System.out.println("[4] Structure only (without file contents) ");
        int choice = readInt("Select option (1-4): ", 1, 4);
        switch (choice) {
            case 1: return AnalysisConfig.OutputFormat.MARKDOWN;
            case 2: return AnalysisConfig.OutputFormat.HTML;
            case 3: return AnalysisConfig.OutputFormat.BOTH;
            case 4: return AnalysisConfig.OutputFormat.STRUCTURE_ONLY;
            default: return AnalysisConfig.OutputFormat.MARKDOWN;
        }
    }

    private String getOutputFileName() {
        return readLine("Output file name (without extension) [default: project-audit]: ", "project-audit");
    }

    private long getMaxFileSizeKB() {
        String input = readLine("Maximum file size to include (in KB, 0 = no limit) [default: 50000]: ", "50000");
        try {
            long size = Long.parseLong(input);
            return Math.max(0, size);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default value 50000 KB. ");
            return 50000;
        }
    }

    // --- Generic input methods using BufferedReader ---
    private String readLine(String prompt, String defaultVal) {
        if (closed) {
            return defaultVal;
        }

        System.out.print(prompt);
        try {
            String input = reader.readLine();
            if (input == null || input.trim().isEmpty()) {
                return defaultVal;
            }
            return input.trim();
        } catch (IOException e) {
            LOGGER.warn("Error reading input, using default value: {}", defaultVal, e);
            return defaultVal;
        }
    }

    private int readInt(String prompt, int min, int max) {
        int value;
        while (true) {
            String input = readLine(prompt, String.valueOf(min));
            try {
                value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    break;
                } else {
                    System.out.println("Value must be between " + min + " and " + max + ". ");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number. ");
            }
        }
        return value;
    }

    private boolean readYesNo(String prompt, boolean defaultYes) {
        String input = readLine(prompt + (defaultYes ? " [Y/n]: " : " [y/N]: "), "");
        if (input.isEmpty()) {
            return defaultYes;
        }
        return input.trim().toLowerCase().startsWith("y");
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            try {
                reader.close();
            } catch (IOException e) {
                LOGGER.debug("Error closing reader", e);
            }
        }
    }
}