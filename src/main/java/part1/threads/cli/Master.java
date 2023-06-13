package part1.threads.cli;

import java.io.File;

public class Master extends Worker {
    private final File rootDirectory;
    private final String wordToFind;

    public Master(File rootDir, String wordToSearch) {
        super("Master-Thread");
        this.rootDirectory = rootDir;
        this.wordToFind = wordToSearch;
    }

    @Override
    public void run() {
        // value fount for the best performance
        //int nCoresForBestPerformance = 3;
        int nCore = Runtime.getRuntime().availableProcessors();
        int nLoaderThreads = nCore;         // IO-intensive
        int nAnalyzerThreads = nCore + 1;   // CPU intensive

        long startTime = System.currentTimeMillis();

        DocumentsCounter docCounter = new DocumentsCounter();   //Monitor per il conteggio delle parole, dei doc trovati e analizzati
        ConcurrentBuffer<File> documentsDiscovered = new ConcurrentBuffer<>();  //Buffer per l'inserimento di ???
        Discoverer discoverer = new Discoverer(this.rootDirectory, documentsDiscovered, docCounter);
        discoverer.start();

        ConcurrentBuffer<Document> documentsLoaded = new ConcurrentBuffer<>();
        MyLatch allDocumentsLoaded = new MyLatch(nLoaderThreads);
        for (int i = 0; i < nLoaderThreads; i++)
            new Loader(i, documentsDiscovered, documentsLoaded, allDocumentsLoaded).start();

        MyLatch allDocumentsAnalyzed = new MyLatch(nAnalyzerThreads);
        for (int i = 0; i < nAnalyzerThreads; i++)
            new Analyzer(i, documentsLoaded, docCounter, this.wordToFind, allDocumentsAnalyzed).start();

        try {
            allDocumentsLoaded.await();
            documentsLoaded.closeBuffer();
            allDocumentsAnalyzed.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\nDuration: " + duration + "ms, with " + nCore + " cores used (" + nLoaderThreads +
                " Loader, " + nAnalyzerThreads + " Analyzer)");
        System.out.println("Documents found: " + docCounter.getDocumentsFound());
        System.out.println("Documents containing the word " + this.wordToFind + ": " + docCounter.getWordOccurrences());
    }
}
