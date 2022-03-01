package part1.tasks.gui;

import part1.tasks.cli.Utils;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Master extends Thread {
    private final File rootDirectory;
    private final String wordToFind;
    private final View view;
    private final TerminationFlag terminationFlag;

    public Master(File rootDir, String wordToFind, View view, TerminationFlag terminationFlag) {
        super("Master-Thread");
        this.rootDirectory = rootDir;
        this.wordToFind = wordToFind;
        this.view = view;
        this.terminationFlag = terminationFlag;
    }

    @Override
    public void run() {
        int nCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorForDiscovering = Executors.newSingleThreadExecutor();
        ExecutorService executorForLoading = Executors.newFixedThreadPool(5);
        ExecutorService executorForAnalyzing = Executors.newFixedThreadPool(5);
        ExecutorService executorForUpdatingGUI = Executors.newSingleThreadExecutor();

        DocumentsCounter documentsCounter = new DocumentsCounter();
        Utils utils = new Utils(executorForLoading, executorForAnalyzing, documentsCounter, terminationFlag, wordToFind);

        long startTime = System.currentTimeMillis();
        System.out.println("Started...");

        Future<List<Future<Future<Void>>>> tasks = executorForDiscovering.submit(new docDiscoveringTask(rootDirectory, utils));
        executorForUpdatingGUI.execute(new ViewUpdateTask(view, utils));

        try {
            for (Future<Future<Void>> docFuture : tasks.get())
                docFuture.get().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        long duration = System.currentTimeMillis() - startTime;

        executorForDiscovering.shutdown();
        executorForLoading.shutdown();
        executorForAnalyzing.shutdown();
        executorForUpdatingGUI.shutdownNow();

        this.view.setComputationDuration(duration);
        this.view.computationDone();

        System.out.println("Done in: " + duration + " ms");
        System.out.println("Documents found: " + documentsCounter.getDocumentsFound());
        System.out.println("Documents analyzed: " + documentsCounter.getDocumentsAnalyzed());
        System.out.println("Word occurrences: " + documentsCounter.getWordOccurrences());
    }
}
