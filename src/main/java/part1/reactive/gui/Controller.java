package part1.reactive.gui;

import part1.threads.gui.InputListener;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

import java.io.File;
import java.util.concurrent.Executors;

public class Controller implements InputListener {
    private final View view;
    private TerminationFlag terminationFlag;

    public Controller(View view) {
        this.view = view;
    }

    @Override
    public void started(File rootDirectory, String wordToFind) {
        this.terminationFlag = new TerminationFlag();
        this.terminationFlag.reset();
        Master master = new Master(rootDirectory, wordToFind, view, terminationFlag);
        var exec = Executors.newSingleThreadExecutor();
        exec.execute(master);
        exec.shutdown();
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
    }
}
