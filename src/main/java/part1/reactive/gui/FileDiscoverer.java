package part1.reactive.gui;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;

import java.io.File;
import java.util.Objects;

public class FileDiscoverer {
    private final File rootDirectory;
    private final TerminationFlag terminationFlag;
    private final DocumentsCounter documentsCounter;

    public FileDiscoverer(File rootDirectory, TerminationFlag terminationFlag, DocumentsCounter documentsCounter) {
        this.rootDirectory = rootDirectory;
        this.terminationFlag = terminationFlag;
        this.documentsCounter = documentsCounter;
    }

    public Observable<File> buildFileStream() {
        return Observable.create(subscriber -> {
            explore(rootDirectory, subscriber);
            subscriber.onComplete();
        });
    }

    private void explore(File rootDirectory, ObservableEmitter<File> subscriber) {
        for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
            if (terminationFlag.canProceed())
                emitFile(file, subscriber);
            else if (terminationFlag.isPaused())
                terminationFlag.waitToBeResumed();
            else
                break;
        }
    }

    private void emitFile(File file, ObservableEmitter<File> subscriber) {
        if (file.isDirectory())
            explore(file, subscriber);
        else if (file.getName().endsWith(".pdf")) {
            subscriber.onNext(file);
            this.documentsCounter.incrementDocumentsFound();
        }
    }
}
