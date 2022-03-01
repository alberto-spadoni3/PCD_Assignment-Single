package part1.threads.gui;


import part1.threads.cli.ConcurrentBuffer;
import part1.threads.cli.DocumentsCounter;
import part1.threads.cli.Worker;

import java.io.File;

public class Discoverer extends Worker {
    private final ConcurrentBuffer<File> documents;
    private final DocumentsCounter docCounter;
    private final File rootDirectory;
    private final TerminationFlag termination;

    public Discoverer(File rootDirectory, ConcurrentBuffer<File> documents, DocumentsCounter docCounter, TerminationFlag termination) {
        super("Discover-Thread");
        this.documents = documents;
        this.rootDirectory = rootDirectory;
        this.docCounter = docCounter;
        this.termination = termination;
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
            if (this.termination.canProceed()) {
                if (file.isDirectory())
                    this.discover(file);
                else if (file.getName().toLowerCase().endsWith(".pdf")) {
                    this.documents.put(file);
                    this.docCounter.incrementDocumentsFound();
                }
            } else if (this.termination.isPaused())
                this.termination.waitToBeResumed();
            else { // the stop button is been pressed
                log("Stopped");
                break;
            }
        }
    }
}
