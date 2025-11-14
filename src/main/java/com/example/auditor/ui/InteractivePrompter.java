// src/main/java/com/example/auditor/ui/InteractivePrompter.java
package com.example.auditor.ui;

import com.example.auditor.core.UserInterface;
import com.example.auditor.model.AnalysisConfig;
import com.example.auditor.utils.ConsoleColors;
import com.example.auditor.utils.ProgressBar; // Предполагается, что ProgressBar уже в utils

import java.io.Console;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner; // Импортируем Scanner

/**
 * Implementation of UserInterface, providing an interactive text interface
 * for obtaining analysis configuration from the user.
 * Uses System.console() if available, otherwise Scanner(System.in).
 */
public class InteractivePrompter implements UserInterface {

    // Убираем поля для Scanner и Console из класса
    // private final Scanner fallbackScanner; // Fallback scanner if console is unavailable
    // private final Console console; // Console for more reliable input

    // Constructor not needed if not initializing fields
    public InteractivePrompter() {
        // Empty constructor
    }

    @Override
    public AnalysisConfig getUserConfig() {
        // Try to get Console and Scanner just before use
        Console console = System.console();
        Scanner fallbackScanner = null;
        if (console == null) {
            // If console is unavailable, use Scanner
            // IMPORTANT: Scanner might not work correctly with Gradle run, but let's try
            fallbackScanner = new Scanner(System.in);
        }

        System.out.println(ConsoleColors.CYAN + "\n--- PROJECT AUDITOR SETUP ---" + ConsoleColors.RESET);

        // STEP 1: PROJECT SELECTION
        System.out.println(ConsoleColors.CYAN + "\nSTEP 1: PROJECT SELECTION" + ConsoleColors.RESET);
        String projectPathStr = readLine("Enter path to project: ", "project-audit", console, fallbackScanner);
        while (projectPathStr.isEmpty()) {
            projectPathStr = readLine("Path cannot be empty. Please re-enter: ", "project-audit", console, fallbackScanner);
        }
        Path projectPath = Paths.get(projectPathStr).toAbsolutePath().normalize();
        System.out.println(" -> Selected project: " + projectPath);

        // STEP 2: OUTPUT CONFIGURATION
        System.out.println(ConsoleColors.CYAN + "\nSTEP 2: OUTPUT CONFIGURATION" + ConsoleColors.RESET);
        AnalysisConfig.OutputFormat outputFormat = getOutputFormat(console, fallbackScanner);
        String outputFileName = getOutputFileName(console, fallbackScanner);
        boolean generateJsonMetadata = readYesNo("Generate JSON metadata file? ", true, console, fallbackScanner);
        boolean openResultsAfterwards = readYesNo("Open results after completion? ", true, console, fallbackScanner);

        // STEP 3: FILTERING SETTINGS
        System.out.println(ConsoleColors.CYAN + "\nSTEP 3: FILTERING SETTINGS" + ConsoleColors.RESET);
        boolean useGitIgnore = readYesNo("Use .gitignore for filtering? ", true, console, fallbackScanner);
        long maxFileSizeKB = getMaxFileSizeKB(console, fallbackScanner);

        // STEP 4: ADDITIONAL OPTIONS
        System.out.println(ConsoleColors.CYAN + "\nSTEP 4: ADDITIONAL OPTIONS" + ConsoleColors.RESET);
        boolean lightMode = outputFormat == AnalysisConfig.OutputFormat.STRUCTURE_ONLY; // Set lightMode based on format
        if (outputFormat != AnalysisConfig.OutputFormat.STRUCTURE_ONLY) {
            lightMode = readYesNo("Use 'Structure Only' mode (ignores file contents)? ", false, console, fallbackScanner);
        }


        // STEP 5: SETTINGS CONFIRMATION
        System.out.println(ConsoleColors.CYAN + "\nSTEP 5: SETTINGS CONFIRMATION" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.YELLOW + "The following settings will be applied:" + ConsoleColors.RESET);
        System.out.println(" • Project: " + projectPath);
        System.out.println(" • Output Format: " + outputFormat);
        System.out.println(" • Output File Name: " + outputFileName);
        System.out.println(" • JSON Metadata: " + (generateJsonMetadata ? "Yes" : "No"));
        System.out.println(" • Open After Completion: " + (openResultsAfterwards ? "Yes" : "No"));
        System.out.println(" • Use .gitignore: " + (useGitIgnore ? "Yes" : "No"));
        System.out.println(" • Max File Size: " + maxFileSizeKB + " KB");
        System.out.println(" • Mode: " + (lightMode ? "Light" : "Full"));

        boolean confirm = readYesNo("\nProceed with these settings? ", true, console, fallbackScanner);

        if (!confirm) {
            System.out.println(ConsoleColors.RED + "Settings rejected by user. Exiting." + ConsoleColors.RESET);
            // Close Scanner before exit if it was created
            if (fallbackScanner != null && console == null) {
                fallbackScanner.close();
            }
            System.exit(0); // Or throw an exception if logic allows
        }

        // Close Scanner before return if it was created
        if (fallbackScanner != null && console == null) {
            fallbackScanner.close();
        }

        // Return the built configuration object
        // Exclude patterns are empty for now, can be added later if console input is needed
        return new AnalysisConfig(
                projectPath,
                outputFormat,
                outputFileName,
                generateJsonMetadata,
                openResultsAfterwards,
                useGitIgnore,
                maxFileSizeKB,
                List.of(), // Currently fixed list
                lightMode
        );
    }

    // --- Helper methods for interaction ---
    private AnalysisConfig.OutputFormat getOutputFormat(Console console, Scanner fallbackScanner) {
        System.out.println("Select output format:");
        System.out.println("[1] Markdown (.md) - for LLM analysis");
        System.out.println("[2] HTML (.html) - for web viewing");
        System.out.println("[3] Both formats");
        System.out.println("[4] Structure only (without file contents)");
        int choice = readInt("Select option (1-4): ", 1, 4, console, fallbackScanner);
        switch (choice) {
            case 1: return AnalysisConfig.OutputFormat.MARKDOWN;
            case 2: return AnalysisConfig.OutputFormat.HTML;
            case 3: return AnalysisConfig.OutputFormat.BOTH;
            case 4: return AnalysisConfig.OutputFormat.STRUCTURE_ONLY;
            default: return AnalysisConfig.OutputFormat.MARKDOWN; // Fallback
        }
    }

    private String getOutputFileName(Console console, Scanner fallbackScanner) {
        return readLine("Output file name (without extension) [default: project-audit]: ", "project-audit", console, fallbackScanner);
    }

    private long getMaxFileSizeKB(Console console, Scanner fallbackScanner) {
        String input = readLine("Maximum file size to include (in KB, 0 = no limit) [default 50000]: ", "50000", console, fallbackScanner);
        try {
            long size = Long.parseLong(input);
            if (size < 0) {
                System.out.println("Size cannot be negative, using 0 (no limit).");
                return 0; // Возвращаем 0 для "без ограничений"
            }
            // Если пользователь ввел 0, оставляем 0. Если ввел > 0, оставляем как есть.
            return size; // Просто возвращаем введённое значение
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default value 50000 KB.");
            return 50000; // Значение по умолчанию
        }
    }

    // --- Generic input methods using Console or Scanner ---
    private String readLine(String prompt, String defaultVal, Console console, Scanner fallbackScanner) {
        if (console != null) {
            // Use console if available
            String input = console.readLine(prompt);
            if (input == null || input.trim().isEmpty()) {
                input = defaultVal;
            }
            return input.trim();
        } else {
            // Use fallback scanner
            System.out.print(prompt);
            String input = fallbackScanner.nextLine();
            if (input == null || input.trim().isEmpty()) {
                input = defaultVal;
            }
            return input.trim();
        }
    }

    private int readInt(String prompt, int min, int max, Console console, Scanner fallbackScanner) {
        int value;
        while (true) {
            String input = readLine(prompt, String.valueOf(min), console, fallbackScanner); // Suggest min as "default" for convenience
            try {
                value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    break;
                } else {
                    System.out.println("Value must be between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        return value;
    }

    private boolean readYesNo(String prompt, boolean defaultYes, Console console, Scanner fallbackScanner) {
        String input = readLine(prompt + (defaultYes ? " [Y/n]: " : " [y/N]: "), "", console, fallbackScanner);
        if (input.isEmpty()) {
            return defaultYes;
        }
        return input.trim().toLowerCase().startsWith("y"); // Accept "y", "yes", "Y", "Yes", ...
    }
}