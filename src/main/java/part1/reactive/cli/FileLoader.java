package part1.reactive.cli;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import part1.threads.cli.Document;
import part1.common.TerminationFlag;

import java.io.File;
import java.io.IOException;

public class FileLoader extends ActiveComponent {
    private PDFTextStripper stripper;
    private final StringBuilder documentText;
    private final TerminationFlag terminationFlag;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public FileLoader() {
        try {
            stripper = new PDFTextStripper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.documentText = new StringBuilder(4096);
        this.terminationFlag = null;
        this.GUIVersion = false;
    }

    public FileLoader(TerminationFlag terminationFlag) {
        try {
            stripper = new PDFTextStripper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        documentText = new StringBuilder(4096);
        this.terminationFlag = terminationFlag;
        this.GUIVersion = true;
    }

    public Observable<Document> buildDocumentsStream(Observable<File> fileStream) {
        return fileStream
            .observeOn(Schedulers.io())
            .flatMap(pdfToLoad -> {
                PDDocument documentLoaded = PDDocument.load(pdfToLoad);
                AccessPermission permission = documentLoaded.getCurrentAccessPermission();
                if (this.GUIVersion) {
                    if (terminationFlag.isNotStopped() && permission.canExtractContent()) {
                        if (terminationFlag.isPaused()) {
                            terminationFlag.waitToBeResumed();
                        }
                        return buildDocObservable(documentLoaded, pdfToLoad);
                    }
                } else {
                    if (permission.canExtractContent()) {
                        return buildDocObservable(documentLoaded, pdfToLoad);
                    }
                }

                // In case the document content cannot be extracted
                documentLoaded.close();
                return Observable.empty();
            });
    }

    private Observable<Document> buildDocObservable(PDDocument documentLoaded, File pdfToLoad) {
        return Observable.create(subscriber -> {
            for (int i = 1; i <= documentLoaded.getNumberOfPages(); i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                documentText.append(stripper.getText(documentLoaded));
            }
            Document doc = new Document(pdfToLoad.getName(), documentText.toString());
            subscriber.onNext(doc);
            log("Loaded " + doc.getName());

            documentLoaded.close();
            documentText.delete(0, documentText.length());
            subscriber.onComplete();
        });
    }

}
