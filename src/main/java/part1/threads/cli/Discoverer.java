package part1.threads.cli;

import part1.threads.gui.TerminationFlag;

import java.io.File;

public class Discoverer extends Worker {
    private final ConcurrentBuffer<File> bufferDoc;
    private final DocumentsCounter monitorDocCounter;
    private final File rootDirectory;
    private TerminationFlag termination;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public Discoverer(File rootDirectory, ConcurrentBuffer<File> documents, DocumentsCounter docCounter) {
        super("Discover-Thread");
        this.bufferDoc = documents;
        this.rootDirectory = rootDirectory;
        this.monitorDocCounter = docCounter;
        this.GUIVersion = false;
    }

    public Discoverer(File rootDirectory, ConcurrentBuffer<File> documents, DocumentsCounter docCounter, TerminationFlag termination) {
        super("Discover-Thread");
        this.bufferDoc = documents;
        this.rootDirectory = rootDirectory;
        this.monitorDocCounter = docCounter;
        this.termination = termination;
        this.GUIVersion = true;
    }

    @Override
    public void run() {
        super.log("Starting PDF discovery in " + this.rootDirectory);
        this.discover(this.rootDirectory);
        this.bufferDoc.closeBuffer();
        super.log("Discovery finished. Found " + this.monitorDocCounter.getDocumentsFound() + " PDF files.");
    }

    private void discover(File rootDirectory) {
        if(this.GUIVersion){
            for (File file : rootDirectory.listFiles()) {
                if (this.termination.canProceed()) {
                    if (file.isDirectory())
                        this.discover(file);
                    else if (file.getName().toLowerCase().endsWith(".pdf")) {
                        this.bufferDoc.put(file);
                        this.monitorDocCounter.incrementDocumentsFound();
                    }
                } else if (this.termination.isPaused())
                    this.termination.waitToBeResumed();
                else { // the stop button is been pressed
                    log("Stopped");
                    break;
                }
            }
        }
        else {
            for (File file : rootDirectory.listFiles()) {
                if (file.isDirectory())
                    this.discover(file);
                else if (file.getName().toLowerCase().endsWith(".pdf")) {
                    this.bufferDoc.put(file);
                    this.monitorDocCounter.incrementDocumentsFound();
                }
            }
        }
    }
}
