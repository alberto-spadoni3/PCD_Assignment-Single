package part1.eventloop.cli;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import part1.threads.cli.DocumentsCounter;
import part1.common.TerminationFlag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileDiscoverer extends AbstractVerticle {
    private final File rootDirectory;
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public FileDiscoverer(File rootDirectory, DocumentsCounter documentsCounter) {
        this.rootDirectory = rootDirectory;
        this.terminationFlag = null;
        this.documentsCounter = documentsCounter;
        this.GUIVersion = false;
    }

    public FileDiscoverer(File rootDirectory, DocumentsCounter documentsCounter, TerminationFlag terminationFlag) {
        this.rootDirectory = rootDirectory;
        this.terminationFlag = terminationFlag;
        this.documentsCounter = documentsCounter;
        this.GUIVersion = true;
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
        if(this.GUIVersion) {
            for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
                if (terminationFlag.canProceed())
                    explore(filesList, file);
                else if (terminationFlag.isPaused())
                    terminationFlag.waitToBeResumed();
                else
                    break;
            }
        } else {
            for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
                explore(filesList, file);
            }
        }
    }

    private void explore(List<String> filesList, File file) {
        if (file.isDirectory())
            this.discoverRecursive(file, filesList);
        else if (file.getName().toLowerCase().endsWith(".pdf")) {
            filesList.add(file.getAbsolutePath());
            documentsCounter.incrementDocumentsFound();
        }
    }
}
