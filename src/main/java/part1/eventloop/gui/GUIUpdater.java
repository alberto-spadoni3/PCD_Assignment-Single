package part1.eventloop.gui;

import io.vertx.core.AbstractVerticle;
import part1.threads.cli.DocumentsCounter;
import part1.common.TerminationFlag;
import part1.common.View;

import java.util.List;

public class GUIUpdater extends AbstractVerticle {
    private final View view;
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;

    public GUIUpdater(View view, DocumentsCounter documentsCounter, TerminationFlag terminationFlag) {
        this.view = view;
        this.documentsCounter = documentsCounter;
        this.terminationFlag = terminationFlag;
    }

    @Override
    public void start() throws Exception {
        vertx.setPeriodic(20, this::updateGUI);
    }

    private void updateGUI(Long id) {
        vertx.executeBlocking( handler -> {
            if (terminationFlag.isNotStopped()) {
                if (terminationFlag.isPaused()) {
                    terminationFlag.waitToBeResumed();
                }
                List<Integer> temp = this.documentsCounter.getDocFoundAnalyzedAndWordOccurences();
                this.view.update(temp.get(0), temp.get(1), temp.get(2));
            } else
                vertx.cancelTimer(id);
        });
    }
}
