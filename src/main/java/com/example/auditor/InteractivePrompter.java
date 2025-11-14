package com.example.auditor;

import com.example.auditor.utils.ConsoleColors;

import java.util.Scanner;

public class InteractivePrompter {

    private final Scanner scanner;
    private final boolean isWindows;

    public InteractivePrompter() {
        this.isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

        // For Windows, use Scanner with explicit UTF-8 encoding
        if (isWindows) {
            this.scanner = new Scanner(System.in, "UTF-8");
        } else {
            this.scanner = new Scanner(System.in, "UTF-8");
        }
    }

    public String readValidatedPath(String prompt, String defaultPath) {
        while (true) {
            String path;
            if (defaultPath != null && !defaultPath.isEmpty()) {
                System.out.print(prompt + " [default: " + defaultPath + "]: ");
                path = scanner.nextLine();
                if (path.trim().isEmpty()) {
                    path = defaultPath;
                }
            } else {
                System.out.print(prompt + ": ");
                path = scanner.nextLine();
            }

            if (path == null || path.trim().isEmpty()) {
                System.err.println(ConsoleColors.RED + "Path cannot be empty!" + ConsoleColors.RESET);
                continue;
            }

            // Convert relative path to absolute
            java.io.File file = new java.io.File(path);
            if (!file.isAbsolute()) {
                file = new java.io.File(System.getProperty("user.dir"), path);
            }

            return file.getAbsolutePath();
        }
    }

    public int showMenu(String title, String[] options, int defaultChoice) {
        System.out.println("\n" + ConsoleColors.YELLOW + title);
        System.out.println(ConsoleColors.YELLOW + "-".repeat(title.length()));

        for (int i = 0; i < options.length; i++) {
            if (i == defaultChoice) {
                System.out.println(ConsoleColors.GREEN + "  [" + (i+1) + "]* " + options[i] + ConsoleColors.RESET);
            } else {
                System.out.println("  [" + (i+1) + "]  " + options[i]);
            }
        }

        while (true) {
            System.out.print("\nSelect option (1-" + options.length + "): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return defaultChoice + 1;
            }

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= options.length) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
            }

            System.err.println(ConsoleColors.RED + "Invalid selection. Please try again." + ConsoleColors.RESET);
        }
    }

    public boolean readYesNo(String question, boolean defaultValue) {
        String defaultText = defaultValue ? "[Y/n]" : "[y/N]";
        while (true) {
            System.out.print(question + " " + defaultText + " ");
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.isEmpty()) {
                return defaultValue;
            }

            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }

            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }

            System.err.println(ConsoleColors.RED + "Please enter Y (Yes) or N (No)" + ConsoleColors.RESET);
        }
    }

    public int readNumber(String prompt, int defaultValue, int min, int max) {
        while (true) {
            System.out.print(prompt + " [default: " + defaultValue + "]: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return defaultValue;
            }

            try {
                int number = Integer.parseInt(input);
                if (number >= min && number <= max) {
                    return number;
                }
                System.err.println(ConsoleColors.RED + "Please enter a number between " + min + " and " + max + ConsoleColors.RESET);
            } catch (NumberFormatException e) {
                System.err.println(ConsoleColors.RED + "Please enter a valid number" + ConsoleColors.RESET);
            }
        }
    }

    public String readString(String prompt, String defaultValue) {
        System.out.print(prompt + " [default: " + defaultValue + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}