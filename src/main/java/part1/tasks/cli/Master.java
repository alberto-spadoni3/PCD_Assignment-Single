package part1.tasks.cli;

import part1.threads.cli.DocumentsCounter;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Master extends Thread {
    private final File rootDirectory;
    private final String wordToFind;

    public Master(File rootDir, String wordToFind) {
        super("Master-Thread");
        this.rootDirectory = rootDir;
        this.wordToFind = wordToFind;
    }

    @Override
    public void run() {
        int nCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorForDiscovering = Executors.newSingleThreadExecutor();
        ExecutorService executorForLoading = Executors.newFixedThreadPool(nCores);
        ExecutorService executorForAnalyzing = Executors.newFixedThreadPool(nCores + 1);

        DocumentsCounter documentsCounter = new DocumentsCounter();
        Utils utils = new Utils(executorForLoading, executorForAnalyzing, documentsCounter, null, wordToFind);

        long startTime = System.currentTimeMillis();
        System.out.println("Started...");

        Future<List<Future<Future<Void>>>> tasks = executorForDiscovering.submit(new docDiscoveringTask(rootDirectory, utils, false));

        try {
            for (Future<Future<Void>> docFuture : tasks.get())
                docFuture.get().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executorForDiscovering.shutdown();
        executorForLoading.shutdown();
        executorForAnalyzing.shutdown();

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Done in: " + duration + " ms");
        System.out.println("Documents found: " + documentsCounter.getDocumentsFound());
        System.out.println("Word occurrences: " + documentsCounter.getWordOccurrences());
    }
}
