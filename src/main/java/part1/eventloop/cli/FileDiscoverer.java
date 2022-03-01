package part1.eventloop.cli;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import part1.threads.cli.DocumentsCounter;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

class FileDiscoverer extends AbstractVerticle {
    private final File rootDirectory;
    private final DocumentsCounter documentsCounter;

    FileDiscoverer(File rootDirectory, DocumentsCounter documentsCounter) {
        this.rootDirectory = rootDirectory;
        this.documentsCounter = documentsCounter;
    }

    @Override
    public void start() {
        List<String> filesList = new ArrayList<>();
        vertx.executeBlocking((Promise<List<String>> promise) -> discover(rootDirectory, filesList, promise),
                              this::publishValidFiles);
    }

    private void publishValidFiles(AsyncResult<List<String>> asyncResult) {
        vertx.eventBus().publish("filename.bus", asyncResult.result());
    }

    private void discover(File rootDirectory, List<String> filesList, Promise<List<String>> promise) {
        discoverRecursive(rootDirectory, filesList);
        promise.complete(filesList);
    }

    private void discoverRecursive(File rootDirectory, List<String> filesList) {
        for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
            if (file.isDirectory())
                this.discoverRecursive(file, filesList);
            else if (file.getName().toLowerCase().endsWith(".pdf")) {
                filesList.add(file.getAbsolutePath());
                documentsCounter.incrementDocumentsFound();
            }
        }
    }
}
