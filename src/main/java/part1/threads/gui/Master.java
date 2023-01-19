package part1.threads.gui;

import part1.threads.cli.*;

import java.io.File;

public class Master extends Worker {
    private final File rootDirectory;
    private final String wordToFind;
    private final TerminationFlag termination;
    private final View view;

    public Master(File rootDir, String wordToFind, TerminationFlag termination, View view) {
        super("Master-Thread");
        this.rootDirectory = rootDir;
        this.wordToFind = wordToFind;
        this.termination = termination;
        this.view = view;
    }

    @Override
    public void run() {
        // value fount for the best performance
        //int nCoresForBestPerformance = 3;
        int nCore = Runtime.getRuntime().availableProcessors();
        int nLoaderThreads = nCore;         // IO-intensive
        int nAnalyzerThreads = nCore + 1;   // CPU intensive

        DocumentsCounter docCounter = new DocumentsCounter();

        long startTime = System.currentTimeMillis();

        // Starting the thread that updates the GUI
        Updater updater = new Updater(this.view, docCounter, this.termination);
        updater.start();

        // Starting the thread that discovers PDF files
        ConcurrentBuffer<File> documentsDiscovered = new ConcurrentBuffer<>();
        Discoverer discoverer = new Discoverer(this.rootDirectory, documentsDiscovered, docCounter, termination);
        discoverer.start();

        // Starting the threads that load the files in RAM
        ConcurrentBuffer<Document> documentsLoaded = new ConcurrentBuffer<>();
        MyLatch allDocumentsLoaded = new MyLatch(nLoaderThreads);
        for (int i = 0; i < nLoaderThreads; i++)
            new Loader(i, documentsDiscovered, documentsLoaded, allDocumentsLoaded, termination).start();

        // Starting the threads that search the word inside the files loaded
        MyLatch allDocumentsAnalyzed = new MyLatch(nAnalyzerThreads);
        for (int i = 0; i < nAnalyzerThreads; i++)
            new Analyzer(i, documentsLoaded, docCounter, this.wordToFind.toLowerCase(), allDocumentsAnalyzed, termination).start();

        try {
            allDocumentsLoaded.await();
            documentsLoaded.closeBuffer();
            allDocumentsAnalyzed.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long duration = System.currentTimeMillis() - startTime;
        this.view.setComputationDuration((int) duration);

        updater.interrupt(); // This invocation stops the thread that updates the GUI
        this.view.computationDone();

        System.out.println("\nDuration: " + duration + "ms, with " + nCore + " cores used (" + nLoaderThreads +
                " Loader, " + nAnalyzerThreads + " Analyzer)");
        System.out.println("Documents found: " + docCounter.getDocumentsFound());
        System.out.println("Documents analyzed: " + docCounter.getDocumentsAnalyzed());
        System.out.println("Documents containing the word " + this.wordToFind + ": " + docCounter.getWordOccurrences());
    }
}
