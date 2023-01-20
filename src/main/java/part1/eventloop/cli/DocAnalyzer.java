package part1.eventloop.cli;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;

import java.util.ArrayList;
import java.util.List;

public class DocAnalyzer extends AbstractVerticle {
    private final String wordToFind;
    private final Promise<Void> computationComplete;
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public DocAnalyzer(String wordToFind, Promise<Void> computationComplete, DocumentsCounter documentsCounter) {
        this.wordToFind = wordToFind;
        this.terminationFlag = null;
        this.computationComplete = computationComplete;
        this.documentsCounter = documentsCounter;
        this.GUIVersion = false;
    }

    public DocAnalyzer(String wordToFind, TerminationFlag terminationFlag, Promise<Void> computationComplete, DocumentsCounter documentsCounter) {
        this.wordToFind = wordToFind;
        this.terminationFlag = terminationFlag;
        this.computationComplete = computationComplete;
        this.documentsCounter = documentsCounter;
        this.GUIVersion = true;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("docContent.bus",
                (Message<List<JsonObject>> docContentsList) -> {
                    List<Future> results = new ArrayList<>();

                    for (JsonObject docContent : docContentsList.body())
                        results.add(this.analyzeDocument(docContent));

                    CompositeFuture
                            .all(results)
                            .onSuccess(handler -> computationComplete.complete());
                });
    }

    private Future<Void> analyzeDocument(JsonObject document) {
        if (this.GUIVersion) {
            return vertx.executeBlocking(promise -> {
                if (terminationFlag.isNotStopped()) {
                    if (terminationFlag.isPaused())
                        terminationFlag.waitToBeResumed();

                    String[] words = this.splitText(document.getString("content"));
                    for (String word : words) {
                        if (wordToFind.equals(word.toLowerCase())) {
                            documentsCounter.incrementWordOccurrence();
                            String s = "Word " + word + " found inside " + document.getString("filename");
                            System.out.println(s);
                            break;
                        }
                    }
                    documentsCounter.incrementDocumentsAnalyzed();
                }
                promise.complete();
            }, false);
        } else {
            return vertx.executeBlocking(promise -> {
                String[] words = this.splitText(document.getString("content"));
                for (String word : words) {
                    if (wordToFind.equals(word.toLowerCase())) {
                        documentsCounter.incrementWordOccurrence();
                        String s = "Word " + wordToFind + " found inside " + document.getString("filename");
                        System.out.println(s);
                        break;
                    }
                }
                documentsCounter.incrementDocumentsAnalyzed();
                promise.complete();
            }, false);
        }
    }

    private String[] splitText(String textToSplit) {
        String regex = "[\\x{201D}\\x{201C}\\s'\", ?.@;:!-]+";
        return textToSplit.split(regex);
    }
}
