package part1.threads.gui;

import part1.common.TerminationFlag;
import part1.common.View;
import part1.threads.cli.DocumentsCounter;
import part1.threads.cli.Worker;

import java.util.List;

public class Updater extends Worker {
    private final View view;
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag termination;
    private final int UPDATE_RATIO = 20; // in milliseconds

    public Updater(View view, DocumentsCounter documentsCounter, TerminationFlag termination) {
        super("View-Updater");
        this.view = view;
        this.documentsCounter = documentsCounter;
        this.termination = termination;
    }

    @Override
    public void run() {
        log("Started");
        while (true) {
            try {
                if (this.termination.isPaused()) {
                    log("Paused");
                    this.termination.waitToBeResumed();
                }

                this.updateView();

                Thread.sleep(this.UPDATE_RATIO);
            } catch (InterruptedException e) {
                log("Done");
                break;
            }
        }
        // the last update before exiting
        this.updateView();
    }

    private void updateView() {
        List<Integer> temp = this.documentsCounter.getDocFoundAnalyzedAndWordOccurences();
        this.view.update(temp.get(0), temp.get(1), temp.get(2));
    }
}
