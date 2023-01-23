package part1.reactive.gui;

import io.reactivex.rxjava3.core.Observable;
import part1.threads.cli.Document;
import part1.threads.cli.DocumentsCounter;
import part1.common.TerminationFlag;
import part1.common.View;
import part1.reactive.cli.FileDiscoverer;
import part1.reactive.cli.FileLoader;
import part1.reactive.cli.DocAnalyzer;

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
                    log("Done in: " + duration + " ms");
                    log("Documents found: " + documentsCounter.getDocumentsFound());
                    log("Word occurrences: " + documentsCounter.getWordOccurrences());
                    terminationFlag.stop();
                    exec.shutdown();
                }, Throwable::printStackTrace);
    }

    private void log(String message) {
        String currentThreadName = Thread.currentThread().getName();
        System.out.println("[" + currentThreadName + "] " + message);
    }
}
