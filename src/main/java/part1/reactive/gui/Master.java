package part1.reactive.gui;

import io.reactivex.rxjava3.core.Observable;
import part1.threads.cli.Document;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

import java.io.File;
import java.util.concurrent.Executors;

public class Master implements Runnable {
    private final File rootDirectory;
    private final String wordToFind;

    private final View view;
    private final TerminationFlag terminationFlag;
    private final DocumentsCounter documentsCounter;

    public Master(File rootDirectory, String wordToFind, View view, TerminationFlag terminationFlag) {
        this.rootDirectory = rootDirectory;
        this.wordToFind = wordToFind;
        this.view = view;
        this.terminationFlag = terminationFlag;
        this.documentsCounter = new DocumentsCounter();
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        Observable<File> fileStream = new FileDiscoverer(rootDirectory, terminationFlag, documentsCounter)
                .buildFileStream();

        Observable<Document> documentsStream = new FileLoader(terminationFlag)
                .buildDocumentsStream(fileStream);

        Observable<Boolean> wordOccurrencesStream = new DocAnalyzer(wordToFind, terminationFlag, documentsCounter)
                .buildWordsStream(documentsStream);

        GUIUpdater updater = new GUIUpdater(view, documentsCounter, terminationFlag);
        var exec = Executors.newSingleThreadExecutor();
        exec.execute(updater);

        wordOccurrencesStream
                .filter(wordOccurrence -> wordOccurrence)
                .count()
                .blockingSubscribe((wordOccurrences) -> {
                    long duration = System.currentTimeMillis() - start;
                    view.setComputationDuration(duration);
                    view.computationDone();
                    exec.shutdownNow();
                    log("Were found a total of " + documentsCounter.getDocumentsFound() + " PDFs");
                    log("The word " + wordToFind + " was found " + wordOccurrences + " times");
                    log("Duration " + duration);
                }, Throwable::printStackTrace);
    }

    private void log(String message) {
        String currentThreadName = Thread.currentThread().getName();
        System.out.println("[" + currentThreadName + "] " + message);
    }
}
