package part1.eventloop.gui;

import io.vertx.core.AbstractVerticle;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

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
                if (terminationFlag.isPaused())
                    terminationFlag.waitToBeResumed();
                view.update(documentsCounter.getDocumentsFound(),
                        documentsCounter.getDocumentsAnalyzed(),
                        documentsCounter.getWordOccurrences());
            } else
                vertx.cancelTimer(id);
        });
    }
}
