package part1.threads.cli;

import part1.threads.gui.TerminationFlag;

public class Analyzer extends Worker {
    private final ConcurrentBuffer<Document> bufferDocLoaded;
    private final DocumentsCounter monitorDocCounter;
    private final String wordToFind;
    private final MyLatch latch;
    private TerminationFlag termination;

    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public Analyzer(int id, ConcurrentBuffer<Document> documentsLoaded, DocumentsCounter docCounter, String wordToFind, MyLatch latch) {
        super("Analyzer-Thread <" + id + ">");
        this.bufferDocLoaded = documentsLoaded;
        this.monitorDocCounter = docCounter;
        this.wordToFind = wordToFind;
        this.latch = latch;
        this.GUIVersion = false;
    }

    public Analyzer (int id, ConcurrentBuffer<Document> documentsLoaded, DocumentsCounter docCounter, String wordToFind, MyLatch latch, TerminationFlag termination){
        super("Analyzer-Thread <" + id + ">");
        this.bufferDocLoaded = documentsLoaded;
        this.monitorDocCounter = docCounter;
        this.wordToFind = wordToFind;
        this.latch = latch;
        this.termination = termination;
        this.GUIVersion = true;
    }

    @Override
    public void run() {
        log("Starts analyzing...");
        if(this.GUIVersion){
            while (this.termination.isNotStopped()) {
                if (this.termination.isPaused()) {
                    super.log("Paused");
                    this.termination.waitToBeResumed();
                }
                else {
                    try {
                        Document documentToAnalyze = this.bufferDocLoaded.get();
                        this.checkWordPresence(documentToAnalyze);
                        this.monitorDocCounter.incrementDocumentsAnalyzed();
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
        else {
            while (true) {
                try {
                    Document documentToAnalyze = this.bufferDocLoaded.get();
                    this.checkWordPresence(documentToAnalyze);
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

    private void checkWordPresence(Document documentToAnalyze) {
        // First let's split the raw text to obtain an array containing only words
        String[] words = this.splitText(documentToAnalyze.getContent());

        // Then start the process of finding the word inside the document
        for (String word : words) {
            if (this.wordToFind.equals(word.toLowerCase())) {
                log("Found the word \"" + this.wordToFind + "\" in the document " + documentToAnalyze.getName());
                this.monitorDocCounter.incrementWordOccurrence();
                break;
            }
        }
    }

    private String[] splitText(String textToSplit) {
        String regex = "[\\x{201D}\\x{201C}\\s'\", ?.@;:!-]+";
        return textToSplit.split(regex);
    }
}
