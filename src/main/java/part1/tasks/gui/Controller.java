package part1.tasks.gui;

import part1.threads.gui.InputListener;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

import java.io.File;

public class Controller implements InputListener {
    private final View view;
    private final TerminationFlag terminationFlag;

    public Controller(View view) {
        this.view = view;
        this.terminationFlag = new TerminationFlag();
    }

    @Override
    public void started(File rootDir, String wordToSearch) {
        this.terminationFlag.reset();
        Master master = new Master(rootDir, wordToSearch, view, terminationFlag);
        master.start();
    }

    @Override
    public void paused() {
        this.terminationFlag.pause();
    }

    @Override
    public void resumed() {
        this.terminationFlag.resume();
    }

    @Override
    public void stopped() {
        this.terminationFlag.stop();
        this.view.changeState("Stopped");
    }
}
