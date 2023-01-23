package part1.reactive.gui;

import part1.threads.cli.DocumentsCounter;
import part1.common.TerminationFlag;
import part1.common.View;

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
        List<Integer> temp = null;
        while (terminationFlag.isNotStopped()) {
            if (terminationFlag.isPaused()) {
                terminationFlag.waitToBeResumed();
            }
            temp = this.documentsCounter.getDocFoundAnalyzedAndWordOccurences();
            this.view.update(temp.get(0), temp.get(1), temp.get(2));
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {}
        }
        temp = this.documentsCounter.getDocFoundAnalyzedAndWordOccurences();
        this.view.update(temp.get(0), temp.get(1), temp.get(2));
    }
}
