package part1.reactive.cli;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import part1.threads.cli.Document;
import part1.threads.cli.DocumentsCounter;

public class DocAnalyzer extends ActiveComponent {
    private final String wordToFind;
    private final DocumentsCounter documentsCounter;

    public DocAnalyzer(String wordToFind, DocumentsCounter documentsCounter) {
        this.wordToFind = wordToFind;
        this.documentsCounter = documentsCounter;
    }

    public Observable<Boolean> buildWordsStream(Observable<Document> source) {
        return source
            .observeOn(Schedulers.computation())
            .flatMap(documentToAnalyze -> {
                log("Start analyzing");
                String[] words = this.splitText(documentToAnalyze.getContent());
                return Observable.create(subscriber -> {
                    boolean wordFound = false;
                    for (String word : words) {
                        if (this.wordToFind.equals(word.toLowerCase())) {
                            log("Found the word \"" + wordToFind + "\" inside " + documentToAnalyze.getName());
                            wordFound = true;
                            break;
                        }
                    }
                    this.documentsCounter.incrementDocumentsAnalyzed();
                    subscriber.onNext(wordFound);
                    subscriber.onComplete();
                });
            });
    }

    private String[] splitText(String textToSplit) {
        String regex = "[\\x{201D}\\x{201C}\\s'\", ?.@;:!-]+";
        return textToSplit.split(regex);
    }
}
