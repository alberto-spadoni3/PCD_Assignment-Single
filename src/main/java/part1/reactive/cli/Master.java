package part1.reactive.cli;

import io.reactivex.rxjava3.core.Observable;
import part1.threads.cli.Document;
import part1.threads.cli.DocumentsCounter;

import java.io.File;

public class Master implements Runnable {
    private final File rootDirectory;
    private final String wordToFind;
    private final DocumentsCounter documentsCounter;

    public Master(File rootDirectory, String wordToFind) {
        this.rootDirectory = rootDirectory;
        this.wordToFind = wordToFind;
        this.documentsCounter = new DocumentsCounter();
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        Observable<File> fileStream = new FileDiscoverer(rootDirectory, documentsCounter)
            .buildFileStream();

        Observable<Document> documentsStream = new FileLoader()
            .buildDocumentsStream(fileStream);

        Observable<Boolean> wordOccurrencesStream = new DocAnalyzer(wordToFind, documentsCounter)
            .buildWordsStream(documentsStream);

        wordOccurrencesStream
            .filter(wordOccurrence -> wordOccurrence)
            .count()
            .blockingSubscribe((wordOccurrences) -> {
                long duration = System.currentTimeMillis() - start;
                log("Done in: " + duration + " ms");
                log("Documents found: " + documentsCounter.getDocumentsFound());
                log("Word occurrences: " + documentsCounter.getWordOccurrences());
                System.exit(0);
            }, Throwable::printStackTrace);
    }

    private void log(String message) {
        String currentThreadName = Thread.currentThread().getName();
        System.out.println("[" + currentThreadName + "] " + message);
    }
}
