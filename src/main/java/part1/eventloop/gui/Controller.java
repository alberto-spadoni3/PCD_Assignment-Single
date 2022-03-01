package part1.eventloop.gui;

import io.vertx.core.Vertx;
import part1.eventloop.cli.Main;
import part1.threads.gui.InputListener;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller implements InputListener {
    private final View view;
    private String masterDeploymentID;
    private final TerminationFlag terminationFlag;

    public Controller(View view) {
        this.view = view;
        this.terminationFlag = new TerminationFlag();
    }

    @Override
    public void started(File rootDirectory, String wordToFind) {
        Master master = new Master(view, wordToFind, rootDirectory.getAbsolutePath(), terminationFlag);
        ExecutorService exec = Executors.newSingleThreadExecutor();
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
