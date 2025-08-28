package util.loading;

import util.TermInstructs;

public class AutoProgressBar {
    private AutoProgressBarThread progressBarThread;

    public AutoProgressBar(String loadingMessage, int steps, int barWidth) {
        progressBarThread = new AutoProgressBarThread(loadingMessage, steps, barWidth);
    }

    public synchronized void setShowPercent(boolean showPercent) {
        progressBarThread.setShowPercent(showPercent);
    }

    public synchronized void cancel() {
        progressBarThread.cancel();
    }

    public synchronized void increment() {
        progressBarThread.increment();
    }

    public void setLoadingMessage(String loadingMessage) {
        progressBarThread.setLoadingMessage(loadingMessage);
    }

    public void setFrameRate(int milliseconds) {
        progressBarThread.setFrameRate(milliseconds);
    }

    public void start() {
        progressBarThread.start();
    }

    public void stop() {
        progressBarThread.done();

        try {
            progressBarThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        TermInstructs.ERASE_LINE();
        TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
        System.out.println(progressBarThread.getDisplay());
    }

    private class AutoProgressBarThread extends Thread {
        private ManualProgressBar progressBar;
        private boolean running = true;
        private int frameRateMillis = 100;

        public AutoProgressBarThread(String loadingMessage, int steps, int barWidth) {
            this.progressBar = new ManualProgressBar(loadingMessage, steps, barWidth);
        }

        public synchronized void setShowPercent(boolean showPercent) {
            progressBar.setShowPercent(showPercent);
        }

        public synchronized void cancel() {
            progressBar.cancel();
        }

        public synchronized void increment() {
            progressBar.increment();
        }

        public void setLoadingMessage(String loadingMessage) {
            progressBar.setLoadingMessage(loadingMessage);
        }

        public void setFrameRate(int milliseconds) {
            frameRateMillis = milliseconds;
        }

        public void done() {
            running = false;
        }

        public synchronized String getDisplay() {
            return progressBar.getDisplay();
        }

        @Override
        public void run() {
            while (running) {
                TermInstructs.ERASE_LINE();
                TermInstructs.MOVE_CURSOR_TO_LINE_BEG();

                String display = progressBar.getDisplay();

                System.out.print(display);

                try {
                    Thread.sleep(frameRateMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
