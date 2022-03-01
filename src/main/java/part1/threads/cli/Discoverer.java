package part1.threads.cli;

import java.io.File;

public class Discoverer extends Worker {
    private final ConcurrentBuffer<File> documents;
    private final DocumentsCounter docCounter;
    private final File rootDirectory;

    public Discoverer(File rootDirectory, ConcurrentBuffer<File> documents, DocumentsCounter docCounter) {
        super("Discover-Thread");
        this.documents = documents;
        this.rootDirectory = rootDirectory;
        this.docCounter = docCounter;
    }

    @Override
    public void run() {
        super.log("Starting PDF discovery in " + this.rootDirectory);
        this.discover(this.rootDirectory);
        this.documents.closeBuffer();
        super.log("Discovery finished. Found " + this.docCounter.getDocumentsFound() + " PDF files.");
    }

    private void discover(File rootDirectory) {
        for (File file : rootDirectory.listFiles()) {
            if (file.isDirectory())
                this.discover(file);
            else if (file.getName().toLowerCase().endsWith(".pdf")) {
                this.documents.put(file);
                this.docCounter.incrementDocumentsFound();
            }
        }
    }
}
