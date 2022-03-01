package part1.tasks.gui;

import part1.tasks.cli.Utils;
import part1.threads.gui.View;

public class ViewUpdateTask implements Runnable {
    private final Utils utils;
    private final View view;

    public ViewUpdateTask(View view, Utils utils) {
        this.view = view;
        this.utils = utils;
    }

    @Override
    public void run() {
        while (this.utils.terminationFlag.isNotStopped()) {
            if (this.utils.terminationFlag.isPaused())
                this.utils.terminationFlag.waitToBeResumed();
            view.update(this.utils.documentsCounter.getDocumentsFound(),
                        this.utils.documentsCounter.getDocumentsAnalyzed(),
                        this.utils.documentsCounter.getWordOccurrences());
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {}
        }
    }
}
