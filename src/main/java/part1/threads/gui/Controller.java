package part1.threads.gui;

import java.io.File;

public class Controller implements InputListener {
    private final View view;
    private final TerminationFlag termination;

    public Controller(View view) {
        this.view = view;
        this.termination = new TerminationFlag();
    }

    @Override
    public void started(File rootDir, String wordToSearch) {
        this.termination.reset();
        Master master = new Master(rootDir, wordToSearch, this.termination, this.view);
        master.start();
    }

    @Override
    public void paused() {
        this.termination.pause();
    }

    @Override
    public void resumed() {
        this.termination.resume();
    }

    @Override
    public void stopped() {
        this.termination.stop();
        this.view.changeState("Stopped");
    }
}
