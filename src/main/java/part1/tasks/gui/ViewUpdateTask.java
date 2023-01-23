package part1.tasks.gui;

import part1.tasks.cli.Utils;
import part1.common.View;

import java.util.List;

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
            if (this.utils.terminationFlag.isPaused()) {
                this.utils.terminationFlag.waitToBeResumed();
            }
            List<Integer> temp = this.utils.documentsCounter.getDocFoundAnalyzedAndWordOccurences();
            this.view.update(temp.get(0), temp.get(1), temp.get(2));
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {}
        }
    }
}
