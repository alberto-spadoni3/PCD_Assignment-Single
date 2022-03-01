package part1.tasks.cli;

import part1.threads.cli.Document;

import java.util.concurrent.Callable;

public class docAnalyzingTask implements Callable<Void> {
    private final Document docContent;
    private final Utils utils;

    public docAnalyzingTask(Document docContent, Utils utils) {
        this.docContent = docContent;
        this.utils = utils;
    }

    @Override
    public Void call() {
        this.checkWordPresence(docContent);
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
