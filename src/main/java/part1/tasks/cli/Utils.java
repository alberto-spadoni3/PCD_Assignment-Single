package part1.tasks.cli;

import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;

import java.util.concurrent.ExecutorService;

public class Utils {
    public final ExecutorService executorForLoading;
    public final ExecutorService executorForAnalyzing;
    public final DocumentsCounter documentsCounter;
    public final TerminationFlag terminationFlag;

    public final String wordToFind;

    public Utils(ExecutorService executorForLoading, ExecutorService executorForAnalyzing, DocumentsCounter documentsCounter, TerminationFlag terminationFlag, String wordToFind) {
        this.executorForLoading = executorForLoading;
        this.executorForAnalyzing = executorForAnalyzing;
        this.documentsCounter = documentsCounter;
        this.terminationFlag = terminationFlag;
        this.wordToFind = wordToFind;
    }
}
