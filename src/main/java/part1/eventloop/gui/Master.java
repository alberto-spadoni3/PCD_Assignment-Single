package part1.eventloop.gui;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import part1.eventloop.cli.Main;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

import java.io.File;

public class Master implements Runnable {
    private final View view;
    private final String wordToFind;
    private final File rootDirectory;
    private final TerminationFlag terminationFlag;
    private final DocumentsCounter documentsCounter;
    private final Vertx vertx;

    public Master(View view, String wordToFind, String rootDirectory, TerminationFlag terminationFlag) {
        this.view = view;
        this.vertx = Vertx.vertx();
        this.wordToFind = wordToFind;
        this.rootDirectory = new File(rootDirectory);
        this.terminationFlag = terminationFlag;
        this.documentsCounter = new DocumentsCounter();
        Main.setMessageCodec(vertx);
    }

    @Override
    public void run() {
        Promise<Void> computationComplete = Promise.promise();
        String[] verticlesID = new String[4];

        long startTime = System.currentTimeMillis();

        vertx.deployVerticle(new FileDiscoverer(rootDirectory, terminationFlag, documentsCounter), h -> verticlesID[0] = h.result());

        vertx.deployVerticle(new FileLoader(terminationFlag), h -> verticlesID[1] = h.result());

        vertx.deployVerticle(new docAnalyzer(wordToFind, terminationFlag, computationComplete, documentsCounter), h -> verticlesID[2] = h.result());

        vertx.deployVerticle(new GUIUpdater(view, documentsCounter, terminationFlag), h -> verticlesID[3] = h.result());

        computationComplete.future().onComplete(handler -> {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Done in: " + duration + " ms");
            System.out.println("Documents found: " + documentsCounter.getDocumentsFound());
            System.out.println("Word occurrences: " + documentsCounter.getWordOccurrences());

            view.setComputationDuration(duration);
            view.computationDone();

            CompositeFuture
                    .all(vertx.undeploy(verticlesID[0]),
                         vertx.undeploy(verticlesID[1]),
                         vertx.undeploy(verticlesID[2]),
                         vertx.undeploy(verticlesID[3]))
                    .onComplete(h -> System.out.println("Verticles undeployed"));
        });
    }
}
