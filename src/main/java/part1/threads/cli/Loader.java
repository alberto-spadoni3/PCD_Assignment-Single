package part1.threads.cli;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class Loader extends Worker {
    private final ConcurrentBuffer<File> documentsDiscovered;
    private final ConcurrentBuffer<Document> documentLoaded;
    private final MyLatch latch;

    public Loader(int id, ConcurrentBuffer<File> docDiscovered, ConcurrentBuffer<Document> docLoaded, MyLatch latch) {
        super("Loader-Thread <" + id + ">");
        this.documentsDiscovered = docDiscovered;
        this.documentLoaded = docLoaded;
        this.latch = latch;
    }

    @Override
    public void run() {
        super.log("Starts loading documents");
        while (true) {
            try {
                File documentToLoad = this.documentsDiscovered.get();
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
