package part1.threads.cli;

public class Analyzer extends Worker {
    private final ConcurrentBuffer<Document> documentsLoaded;
    private final DocumentsCounter docCounter;
    private final String wordToFind;
    private final MyLatch latch;

    public Analyzer(int id, ConcurrentBuffer<Document> documentsLoaded, DocumentsCounter docCounter, String wordToFind, MyLatch latch) {
        super("Analyzer-Thread <" + id + ">");
        this.documentsLoaded = documentsLoaded;
        this.docCounter = docCounter;
        this.wordToFind = wordToFind;
        this.latch = latch;
    }

    @Override
    public void run() {
        log("Starts analyzing...");
        while (true) {
            try {
                Document documentToAnalyze = this.documentsLoaded.get();
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
