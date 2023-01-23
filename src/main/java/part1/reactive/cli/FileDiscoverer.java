package part1.reactive.cli;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import part1.threads.cli.DocumentsCounter;
import part1.common.TerminationFlag;

import java.io.File;
import java.util.Objects;

public class FileDiscoverer {
    private final File rootDirectory;
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI


    public FileDiscoverer(File rootDirectory, DocumentsCounter documentsCounter) {
        this.rootDirectory = rootDirectory;
        this.documentsCounter = documentsCounter;
        this.terminationFlag = null;
        this.GUIVersion = false;
    }

    public FileDiscoverer(File rootDirectory, TerminationFlag terminationFlag, DocumentsCounter documentsCounter) {
        this.rootDirectory = rootDirectory;
        this.documentsCounter = documentsCounter;
        this.terminationFlag = terminationFlag;
        this.GUIVersion = true;
    }

    public Observable<File> buildFileStream() {
        return Observable.create(subscriber -> {
            explore(rootDirectory, subscriber);
            subscriber.onComplete();
        });
}

    private void explore(File rootDirectory, ObservableEmitter<File> subscriber) {
        if (this.GUIVersion) {
            for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
                if (terminationFlag.canProceed()) {
                    if (file.isDirectory())
                        explore(file, subscriber);
                    else if (file.getName().endsWith(".pdf")) {
                        subscriber.onNext(file);
                        this.documentsCounter.incrementDocumentsFound();
                    }
                }
                else if (terminationFlag.isPaused()) {
                    terminationFlag.waitToBeResumed();
                } else
                    break;
            }
        } else {
            for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
                if (file.isDirectory())
                    explore(file, subscriber);
                else if (file.getName().endsWith(".pdf")) {
                    subscriber.onNext(file);
                    this.documentsCounter.incrementDocumentsFound();
                }
            }
        }
    }
}
