package part1.threads.cli;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import part1.threads.gui.TerminationFlag;

import java.io.File;
import java.io.IOException;

public class Loader extends Worker {
    private final ConcurrentBuffer<File> bufferDocDiscovered;
    private final ConcurrentBuffer<Document> bufferDocLoaded;
    private final MyLatch latch;
    private TerminationFlag termination;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public Loader(int id, ConcurrentBuffer<File> docDiscovered, ConcurrentBuffer<Document> docLoaded, MyLatch latch) {
        super("Loader-Thread <" + id + ">");
        this.bufferDocDiscovered = docDiscovered;
        this.bufferDocLoaded = docLoaded;
        this.latch = latch;
        this.GUIVersion = false;
    }

    public Loader(int id, ConcurrentBuffer<File> docDiscovered, ConcurrentBuffer<Document> docLoaded, MyLatch latch, TerminationFlag termination) {
        super("Loader-Thread <" + id + ">");
        this.bufferDocDiscovered = docDiscovered;
        this.bufferDocLoaded = docLoaded;
        this.latch = latch;
        this.termination = termination;
        this.GUIVersion = true;
    }

    @Override
    public void run() {
        super.log("Starts loading documents");
        if(this.GUIVersion){
            while (this.termination.isNotStopped()) {
                if (this.termination.isPaused()) {
                    super.log("Paused");
                    this.termination.waitToBeResumed();
                }
                else {
                    try {
                        File documentToLoad = this.bufferDocDiscovered.get();
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
        else {
            while (true) {
                try {
                    File documentToLoad = this.bufferDocDiscovered.get();
                    try {
                        this.loadDocument(documentToLoad);
                    } catch (IOException e) {
                        System.out.println("Problem in loading " + documentToLoad);
                    }
                } catch (Exception e) {
                    if (e instanceof BufferClosedException)
                        break;
                    else
                        e.printStackTrace();
                }
            }
            this.latch.countDown();
            log("Done.");
        }
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
        this.bufferDocLoaded.put(doc);
        documentLoaded.close();
    }
}
