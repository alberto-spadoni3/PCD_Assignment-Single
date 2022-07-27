package part1.threads.cli;

import java.util.Arrays;
import java.util.List;

public class DocumentsCounter {
    private int documentsWithWord;
    private int documentsFound;
    private int documentsAnalyzed;

    public DocumentsCounter() {
        this.documentsFound = 0;
        this.documentsWithWord = 0;
        this.documentsAnalyzed = 0;
    }

    public synchronized void incrementWordOccurrence() {
        this.documentsWithWord++;
    }

    public synchronized int getWordOccurrences() {
        return this.documentsWithWord;
    }

    public synchronized void incrementDocumentsFound() {
        this.documentsFound++;
    }

    public synchronized int getDocumentsFound() {
        return this.documentsFound;
    }

    public synchronized void incrementDocumentsAnalyzed() {
        this.documentsAnalyzed++;
    }

    public synchronized int getDocumentsAnalyzed() {
        return this.documentsAnalyzed;
    }

    public synchronized List<Integer> getDocFoundAnalyzedAndWordOccurences() {
        return Arrays.asList(this.documentsFound, this.documentsAnalyzed, this.documentsWithWord);
    }
}
