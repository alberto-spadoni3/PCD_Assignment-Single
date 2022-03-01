package part1.threads.gui;


import part1.threads.cli.*;

public class Analyzer extends Worker {
    private final ConcurrentBuffer<Document> documentsLoaded;
    private final DocumentsCounter docCounter;
    private final String wordToFind;
    private final MyLatch latch;
    private final TerminationFlag termination;

    public Analyzer(int id, ConcurrentBuffer<Document> documentsLoaded, DocumentsCounter docCounter, String wordToFind, MyLatch latch, TerminationFlag termination) {
        super("Analyzer-Thread <" + id + ">");
        this.documentsLoaded = documentsLoaded;
        this.docCounter = docCounter;
        this.wordToFind = wordToFind;
        this.latch = latch;
        this.termination = termination;
    }

    @Override
    public void run() {
        log("Starts analyzing...");
        while (this.termination.isNotStopped()) {
            if (this.termination.isPaused()) {
                super.log("Paused");
                this.termination.waitToBeResumed();
            }
            else {
                try {
                    Document documentToAnalyze = this.documentsLoaded.get();
                    this.checkWordPresence(documentToAnalyze);
                    this.docCounter.incrementDocumentsAnalyzed();
                } catch (Exception e) {
                    if (e instanceof BufferClosedException)
                        break;
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

    private void checkWordPresence(Document documentToAnalyze) {
        // First let's split the raw text to obtain an array containing only words
        String[] words = this.splitText(documentToAnalyze.getContent());

        // Then start the process of finding the word inside the document
        for (String word : words) {
            if (this.wordToFind.equals(word.toLowerCase())) {
                log("Found the word \"" + this.wordToFind + "\" in the document " + documentToAnalyze.getName());
                this.docCounter.incrementWordOccurrence();
                break;
            }
        }
    }

    private String[] splitText(String textToSplit) {
        String regex = "[\\x{201D}\\x{201C}\\s'\", ?.@;:!-]+";
        return textToSplit.split(regex);
    }
}
