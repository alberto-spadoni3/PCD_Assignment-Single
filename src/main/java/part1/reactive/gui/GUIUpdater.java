package part1.reactive.gui;

import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

import java.util.List;

public class GUIUpdater implements Runnable {
    private final View view;
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;

    public GUIUpdater(View view, DocumentsCounter documentsCounter, TerminationFlag terminationFlag) {
        this.view = view;
        this.documentsCounter = documentsCounter;
        this.terminationFlag = terminationFlag;
    }

    @Override
    public void run() {
        while (terminationFlag.isNotStopped()) {
            if (terminationFlag.isPaused()) {
                terminationFlag.waitToBeResumed();
            }
            List<Integer> temp = this.documentsCounter.getDocFoundAnalyzedAndWordOccurences();
            this.view.update(temp.get(0), temp.get(1), temp.get(2));
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {}
        }
    }
}
