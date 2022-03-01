package part1.threads.gui;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import part1.threads.cli.BufferClosedException;
import part1.threads.cli.ConcurrentBuffer;
import part1.threads.cli.Document;
import part1.threads.cli.Worker;

import java.io.File;
import java.io.IOException;

public class Loader extends Worker {
    private final ConcurrentBuffer<File> documentsDiscovered;
    private final ConcurrentBuffer<Document> documentLoaded;
    private final MyLatch latch;
    private final TerminationFlag termination;

    public Loader(int id, ConcurrentBuffer<File> docDiscovered, ConcurrentBuffer<Document> docLoaded, MyLatch latch, TerminationFlag termination) {
        super("Loader-Thread <" + id + ">");
        this.documentsDiscovered = docDiscovered;
        this.documentLoaded = docLoaded;
        this.latch = latch;
        this.termination = termination;
    }

    @Override
    public void run() {
        super.log("Starts loading documents");
        while (this.termination.isNotStopped()) {
            if (this.termination.isPaused()) {
                super.log("Paused");
                this.termination.waitToBeResumed();
            }
            else {
                try {
                    File documentToLoad = this.documentsDiscovered.get();
                    this.loadDocument(documentToLoad);
                } catch (Exception e) {
                    if (e instanceof BufferClosedException)
                        break;
                    else if (e instanceof IOException)
                        System.out.println("Problem in loading a document");
                    else
                        e.printStackTrace();
                }
            }
        }
        this.latch.countDown();
        if (this.termination.isNotStopped())
            log("Done");
        else
            log("Stopped");
    }

    private void loadDocument(File file) throws IOException {
        PDDocument documentLoaded = PDDocument.load(file);
        AccessPermission permission = documentLoaded.getCurrentAccessPermission();
        if (!permission.canExtractContent())
            throw new IOException("You do not have permission to extract text.");

        // Extracting pages
        PDFTextStripper stripper = new PDFTextStripper();
        StringBuilder documentText = new StringBuilder(4096);
        for (int i = 1; i <= documentLoaded.getNumberOfPages(); i++) {
            stripper.setStartPage(i);
            stripper.setEndPage(i);
            documentText.append(stripper.getText(documentLoaded));
        }
        Document doc = new Document(file.getName(), documentText.toString());
        this.documentLoaded.put(doc);
        documentLoaded.close();
    }
}
