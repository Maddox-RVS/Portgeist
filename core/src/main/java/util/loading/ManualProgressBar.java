package util.loading;

import util.Colors;

public class ManualProgressBar {
    private int totalSteps;
    private int currentStep;
    private int barWidth;
    private String loadingMessage;
    private boolean showPercent;
    private boolean cancelled;

    public ManualProgressBar(String loadingMessage, int steps, int barWidth) {
        this.totalSteps = steps;
        this.currentStep = 0;
        this.barWidth = barWidth;
        this.loadingMessage = loadingMessage;
        this.showPercent = false;
        this.cancelled = false;
    }

    public void setShowPercent(boolean showPercent) {
        this.showPercent = showPercent;
    }

    public void setLoadingMessage(String loadingMessage) {
        this.loadingMessage = loadingMessage;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void skip() {
        totalSteps--;
    }

    public void increment() {
        if (currentStep < totalSteps) currentStep++;
    }

    public boolean isDone() {
        return currentStep >= totalSteps;
    }

    public String getDisplay() {
        double percent = currentStep / (double) totalSteps;
        int progress = (int) (percent * barWidth);

        StringBuilder sb = new StringBuilder();
        sb.append("<");

        sb.append(Colors.GREEN);
        for (int i = 0; i < progress; i++) {
            sb.append("=");
        }
        sb.append(Colors.RESET);
        for (int i = 0; i < barWidth - progress; i++) {
            sb.append("-");
        }
        sb.append("> ");

        if (showPercent) {
            sb.append((int) (percent * 100)).append("%");
        }

        sb.append(" ").append(loadingMessage);

        if (cancelled) {
            sb.append(Colors.RED + " [Cancelled]" + Colors.RESET);
        }

        return sb.toString();
    }
}
