package part1.tasks.cli;

import part1.threads.cli.Document;

import java.util.concurrent.Callable;

public class docAnalyzingTask implements Callable<Void> {
    private final Document docContent;
    private final Utils utils;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public docAnalyzingTask(Document docContent, Utils utils, boolean GUIVersion) {
        this.docContent = docContent;
        this.utils = utils;
        this.GUIVersion = GUIVersion;
    }

    @Override
    public Void call() {
        if(this.GUIVersion) {
            if (this.utils.terminationFlag.isPaused())
                this.utils.terminationFlag.waitToBeResumed();
            this.checkWordPresence(docContent);
            this.utils.documentsCounter.incrementDocumentsAnalyzed();
        }
        else {
            this.checkWordPresence(docContent);
        }
        return null;
    }

    private void checkWordPresence(Document documentToAnalyze) {
        // First let's split the raw text to obtain an array containing only words
        String[] words = this.splitText(documentToAnalyze.getContent());

        // Then start the process of finding the word inside the document
        for (String word : words) {
            if (this.utils.wordToFind.equals(word.toLowerCase())) {
                this.utils.documentsCounter.incrementWordOccurrence();
                System.out.println("found word " + utils.wordToFind + " inside " + documentToAnalyze.getName());
                break;
            }
        }
    }

    private String[] splitText(String textToSplit) {
        String regex = "[\\x{201D}\\x{201C}\\s'\", ?.@;:!-]+";
        return textToSplit.split(regex);
    }
}
