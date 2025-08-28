package util.loading;

import util.TermInstructs;

public class Spinner {
    private SpinnerThread spinnerThread;

    public Spinner(String loadingMessage, String completionMessage) {
        spinnerThread = new SpinnerThread(loadingMessage, completionMessage);
    }

    public Spinner(String loadingMessage) {
        this(loadingMessage, "Done");
    }

    public void setFrameRate(int milliseconds) {
        spinnerThread.setFrameRate(milliseconds);
    }

    public void start() {
        spinnerThread.start();
    }

    public void stop() {
        spinnerThread.done();
        
        try {
            spinnerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void setLoadingMessage(String loadingMessage) {
        spinnerThread.setLoadingMessage(loadingMessage);
    }

    private class SpinnerThread extends Thread {
        private int currentIndex = 0;
        private boolean running = true;
        
        private final char[] spinnerChars = {'|', '/', '-', '\\'};
        private String loadingMessage;
        private String completionMessage;

        private int frameRateMillis = 100;

        public SpinnerThread(String loadingMessage, String completionMessage) {
            this.loadingMessage = loadingMessage + ": ";
            this.completionMessage = completionMessage;
        }

        public void done() {
            running = false;
        }

        public void setFrameRate(int milliseconds) {
            frameRateMillis = milliseconds;
        }

        public void setLoadingMessage(String loadingMessage) {
            this.loadingMessage = loadingMessage + ": ";
        }

        @Override
        public void run() {
            while (running) {
                TermInstructs.ERASE_LINE();
                TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                System.out.print(loadingMessage + spinnerChars[currentIndex]);
                currentIndex = (currentIndex + 1) % spinnerChars.length;

                try {
                    Thread.sleep(frameRateMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            TermInstructs.ERASE_LINE();
            TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
            System.out.println(loadingMessage + completionMessage);
        }
    }
}