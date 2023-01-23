package part1.reactive.cli;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import part1.threads.cli.Document;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;

public class DocAnalyzer extends ActiveComponent {
    private final String wordToFind;
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public DocAnalyzer(String wordToFind, DocumentsCounter documentsCounter) {
        this.wordToFind = wordToFind;
        this.documentsCounter = documentsCounter;
        this.terminationFlag = null;
        this.GUIVersion = false;
    }

    public DocAnalyzer(String wordToFind, TerminationFlag terminationFlag, DocumentsCounter documentsCounter) {
        this.wordToFind = wordToFind;
        this.documentsCounter = documentsCounter;
        this.terminationFlag = terminationFlag;
        this.GUIVersion = true;
    }

    public Observable<Boolean> buildWordsStream(Observable<Document> source) {
        if (this.GUIVersion) {
            return source
                    .observeOn(Schedulers.computation())
                    .flatMap(documentToAnalyze -> {
                        //log("Start analyzing");
                        if (terminationFlag.isNotStopped()) {
                            if (terminationFlag.isPaused()) {
                                terminationFlag.waitToBeResumed();
                            }
                            return buildResult(documentToAnalyze);
                        }
                        else
                            log("Analyzer stopped");

                        return Observable.empty();
                    });
        } else {
            return source
                    .observeOn(Schedulers.computation())
                    .flatMap(documentToAnalyze -> {
                        //log("Start analyzing");
                        return buildResult(documentToAnalyze);
                    });
        }
    }

    private Observable<Boolean> buildResult(Document documentToAnalyze) {
        String[] words = this.splitText(documentToAnalyze.getContent());
        return Observable.create(subscriber -> {
            boolean wordFound = false;
            for (String word : words) {
                if (this.wordToFind.equals(word.toLowerCase())) {
                    log("Word " + wordToFind + " found inside " + documentToAnalyze.getName());
                    this.documentsCounter.incrementWordOccurrence();
                    wordFound = true;
                    break;
                }
            }
            this.documentsCounter.incrementDocumentsAnalyzed();
            subscriber.onNext(wordFound);
            subscriber.onComplete();
        });
    }

    private String[] splitText(String textToSplit) {
        String regex = "[\\x{201D}\\x{201C}\\s'\", ?.@;:!-]+";
        return textToSplit.split(regex);
    }
}
