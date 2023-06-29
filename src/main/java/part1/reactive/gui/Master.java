package part1.reactive.gui;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import part1.threads.cli.Document;
import part1.threads.cli.DocumentsCounter;
import part1.common.TerminationFlag;
import part1.common.View;
import part1.reactive.cli.FileDiscoverer;
import part1.reactive.cli.FileLoader;
import part1.reactive.cli.DocAnalyzer;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

        Observable<Long> GUIUpdaterStream = Observable.interval(20, TimeUnit.MILLISECONDS, Schedulers.single());
        GUIUpdaterStream
                .takeUntil(__ -> !terminationFlag.isNotStopped())
                .subscribe(__ -> this.updateGUI());

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
                }, Throwable::printStackTrace);
    }

    private void log(String message) {
        String currentThreadName = Thread.currentThread().getName();
        System.out.println("[" + currentThreadName + "] " + message);
    }

    private void updateGUI() {
        List<Integer> temp = documentsCounter.getDocFoundAnalyzedAndWordOccurences();
        view.update(temp.get(0), temp.get(1), temp.get(2));
    }
}
