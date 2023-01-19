package part1.threads.cli;

import java.io.File;

public class MasterSequential extends Worker {
    private final File rootDirectory;
    private String wordToSearch;

    public MasterSequential(File rootDir, String wordToSearch) {
        super("Master-Thread");
        this.rootDirectory = rootDir;
        this.wordToSearch = wordToSearch;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        DocumentsCounter docCounter = new DocumentsCounter();
        ConcurrentBuffer<File> documentsDiscovered = new ConcurrentBuffer<>();
        Discoverer discoverer = new Discoverer(this.rootDirectory, documentsDiscovered, docCounter);
        discoverer.run();

        MyLatch allDocumentsLoaded = new MyLatch(0);
        MyLatch allDocumentsAnalyzed = new MyLatch(0);

        ConcurrentBuffer<Document> documentsLoaded = new ConcurrentBuffer<>();
        new Loader(0, documentsDiscovered, documentsLoaded, allDocumentsLoaded).run();

        documentsLoaded.closeBuffer();

        new Analyzer(0, documentsLoaded, docCounter, this.wordToSearch, allDocumentsAnalyzed).run();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Duration seq: " + duration);
        System.out.println("Documents found: " + docCounter.getDocumentsFound());
        System.out.println("Documents containing the word " + this.wordToSearch + ": " + docCounter.getWordOccurrences());
    }
}
