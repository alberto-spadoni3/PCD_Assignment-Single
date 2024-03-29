package part1.eventloop.cli;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import part1.common.TerminationFlag;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FileLoader extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(FileLoader.class);
    private final TerminationFlag terminationFlag;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public FileLoader() {
        this.terminationFlag = null;
        this.GUIVersion = false;
    }
    public FileLoader(TerminationFlag terminationFlag) {
        this.terminationFlag = terminationFlag;
        this.GUIVersion = true;
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
        if (this.GUIVersion) {
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
                    documentLoaded.close();
                    promise.complete();
                } catch (IOException e) {
                    promise.fail(e);
                }
                // il parametro false fa si che molteplici chiamate della executeBlocking
                // vengano eseguite in parallelo e non in maniera sequenziale
            },false);
        } else {
            return vertx.executeBlocking(promise -> {
                try {
                    System.out.println("Loading " + fileToLoad);
                    PDDocument documentLoaded = PDDocument.load(new File(fileToLoad));
                    AccessPermission permission = documentLoaded.getCurrentAccessPermission();
                    if (permission.canExtractContent()) {
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
                    documentLoaded.close();
                } catch (IOException e) {
                    log.error("Something went wrong with PDF " + fileToLoad);
                    promise.fail(e);
                }
                // il parametro false fa si che molteplici chiamate della executeBlocking
                // vengano eseguite in parallelo e non in maniera sequenziale
            },false);
        }
    }

    private void publishDocContents(CompositeFuture docContents) {
        List<JsonObject> docContentsList = docContents.result().list();
        vertx.eventBus().publish("docContent.bus",  docContentsList);
    }
}
