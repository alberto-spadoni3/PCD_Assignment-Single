package part1.eventloop.gui;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import part1.threads.gui.TerminationFlag;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FileLoader extends AbstractVerticle {
    private final TerminationFlag terminationFlag;

    public FileLoader(TerminationFlag terminationFlag) {
        this.terminationFlag = terminationFlag;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("filename.bus",
                (Message<List<String>> message) -> {
                    List<Future> results = new LinkedList<>();
                    for (String filePath : message.body())
                        results.add(this.loadDocument(filePath));

                    CompositeFuture
                            .all(results)
                            .onSuccess(this::publishDocContents);
                });
    }

    private Future<JsonObject> loadDocument(String fileToLoad) {
        return vertx.executeBlocking(promise -> {
            try {
                System.out.println("Loading " + fileToLoad);
                PDDocument documentLoaded = PDDocument.load(new File(fileToLoad));
                AccessPermission permission = documentLoaded.getCurrentAccessPermission();
                if (terminationFlag.isNotStopped() && permission.canExtractContent()) {
                    if (terminationFlag.isPaused())
                        terminationFlag.waitToBeResumed();
                    PDFTextStripper stripper = new PDFTextStripper();
                    StringBuilder documentText = new StringBuilder(4096);
                    for (int i = 1; i <= documentLoaded.getNumberOfPages(); i++) {
                        stripper.setStartPage(i);
                        stripper.setEndPage(i);
                        documentText.append(stripper.getText(documentLoaded));
                    }
                    JsonObject document = new JsonObject();
                    document
                        .put("filename", fileToLoad)
                        .put("content", documentText.toString());
                    System.out.println("Doc " + document.getString("filename") + " loaded");
                    promise.complete(document);
                }
                promise.complete();
                documentLoaded.close();
            } catch (IOException e) {
                promise.fail(e);
            }
            // il parametro false fa si che molteplici chiamate della executeBlocking
            // vengano eseguite in parallelo e non in maniera sequenziale
        },false);
    }

    private void publishDocContents(CompositeFuture docContents) {
        List<JsonObject> docContentsList = docContents.result().list();
        vertx.eventBus().publish("docContent.bus",  docContentsList);
    }
}
