package part1.reactive.cli;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;

import java.io.File;
import java.util.Objects;

public class FileDiscoverer {
    private final File rootDirectory;
    private final DocumentsCounter documentsCounter;

    public FileDiscoverer(File rootDirectory, DocumentsCounter documentsCounter) {
        this.rootDirectory = rootDirectory;
        this.documentsCounter = documentsCounter;
    }

    public Observable<File> buildFileStream() {
        Observable<File> fileStream = Observable.create(subscriber -> {
            explore(rootDirectory, subscriber);
            subscriber.onComplete();
        });
        return fileStream.filter(fileName -> fileName.getName().endsWith(".pdf"));
}

    private void explore(File rootDirectory, ObservableEmitter<File> subscriber) {
        for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
            if (file.isDirectory())
                explore(file, subscriber);
            else {
                subscriber.onNext(file);
                this.documentsCounter.incrementDocumentsFound();
            }
        }
    }
}
