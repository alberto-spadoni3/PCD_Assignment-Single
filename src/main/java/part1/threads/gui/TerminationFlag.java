package part1.threads.gui;

public class TerminationFlag {
    private boolean stopped;
    private boolean paused;

    public TerminationFlag() {}

    public void reset() {
        this.stopped = false;
        this.paused = false;
    }

    public synchronized void stop() {
        this.stopped = true;
    }

    public synchronized void pause() {
        this.paused = true;
    }

    public synchronized boolean isNotStopped() {
        return !this.stopped;
    }

    public synchronized boolean isPaused() {
        return this.paused;
    }

    public synchronized boolean canProceed() {
        return this.isNotStopped() && !this.isPaused();
    }

    public synchronized void waitToBeResumed() {
        try {
            while (this.isPaused())
                wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void resume() {
        if (this.isPaused()) {
            this.paused = false;
            notifyAll();
        }
    }
}
