package part1.eventloop.cli;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import part1.threads.cli.DocumentsCounter;
import java.io.File;

public class Master extends AbstractVerticle {
    private final String wordToFind;
    private final File rootDirectory;
    private final DocumentsCounter documentsCounter;

    public Master(String wordToFind, String rootDirectory) {
        this.wordToFind = wordToFind;
        this.rootDirectory = new File(rootDirectory);
        this.documentsCounter = new DocumentsCounter();
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Promise<Void> computationComplete = Promise.promise();
        String[] verticlesID = new String[3];

        long startTime = System.currentTimeMillis();

        vertx.deployVerticle(new FileDiscoverer(rootDirectory, documentsCounter))
                .onSuccess(h -> verticlesID[0] = deploymentID());

        vertx.deployVerticle(new FileLoader())
                .onSuccess(h -> verticlesID[1] = deploymentID());

        vertx.deployVerticle(new DocAnalyzer(wordToFind, computationComplete, documentsCounter))
                .onSuccess(h -> verticlesID[2] = deploymentID());

        computationComplete.future().onComplete(handler -> {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Done in: " + duration + " ms");
            System.out.println("Documents found: " + documentsCounter.getDocumentsFound());
            System.out.println("Word occurrences: " + documentsCounter.getWordOccurrences());

            CompositeFuture
                    .all(vertx.undeploy(verticlesID[0]),
                         vertx.undeploy(verticlesID[1]),
                         vertx.undeploy(verticlesID[2]))
                    .onComplete(h -> System.exit(0));
        });
    }
}
