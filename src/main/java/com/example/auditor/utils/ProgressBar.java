package com.example.auditor.utils;

public class ProgressBar {

    private final String taskName;
    private final int totalSteps;
    private int currentStep = 0;
    private long startTime;
    private boolean finished = false;

    public ProgressBar(String taskName, int totalSteps) {
        this.taskName = taskName;
        this.totalSteps = totalSteps;
        this.startTime = System.currentTimeMillis();
        update(0);
    }

    public synchronized void update(int currentStep) {
        this.currentStep = currentStep;

        if (finished) return;

        long elapsed = System.currentTimeMillis() - startTime;
        double percent = (double) currentStep / totalSteps * 100;
        String bar = getProgressBar(percent);

        // Очистка предыдущей строки
        System.out.print("\r" + " ".repeat(80) + "\r");

        System.out.printf("%-25s [%-50s] %5.1f%% (%d/%d)",
                taskName,
                bar,
                percent,
                currentStep,
                totalSteps);
    }

    public synchronized void finish() {
        if (finished) return;

        finished = true;
        update(totalSteps);

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("\n%s завершен за %.2f сек\n",
                taskName,
                elapsed / 1000.0);
    }

    private String getProgressBar(double percent) {
        int length = 50;
        int progressChars = (int) (length * percent / 100);
        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < length; i++) {
            if (i < progressChars) {
                bar.append("=");
            } else if (i == progressChars && percent < 100) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }

        return bar.toString();
    }
}