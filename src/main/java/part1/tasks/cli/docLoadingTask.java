package part1.tasks.cli;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import part1.threads.cli.Document;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class docLoadingTask implements Callable<Future<Void>> {
    private final File docToLoad;
    private final Utils utils;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public docLoadingTask(File docToLoad, Utils utils, boolean GUIVersion) {
        this.docToLoad = docToLoad;
        this.utils = utils;
        this.GUIVersion = GUIVersion;
    }

    @Override
    public Future<Void> call() {
        Future<Void> task = null;
        if (this.GUIVersion) {
            try {
                if (this.utils.terminationFlag.isNotStopped()) {
                    if (this.utils.terminationFlag.isPaused())
                        this.utils.terminationFlag.waitToBeResumed();
                    task = this.loadDocument(docToLoad, true);
                }
            } catch (IOException e) {
                System.out.println("Problem in loading " + docToLoad);
            }
        }
        else {
            try {
                task = this.loadDocument(docToLoad, false);
            } catch (IOException e) {
                System.out.println("Problem in loading " + docToLoad);
            }
        }
        return task;
    }

    private Future<Void> loadDocument(File file, boolean GUIVersion) throws IOException {
        PDDocument documentLoaded = PDDocument.load(file);
        AccessPermission permission = documentLoaded.getCurrentAccessPermission();
        if (!permission.canExtractContent())
            throw new IOException("You do not have permission to extract text.");

        // Extracting pages
        PDFTextStripper stripper = new PDFTextStripper();
        StringBuilder documentText = new StringBuilder(4096);
        for (int i = 1; i <= documentLoaded.getNumberOfPages(); i++) {
            stripper.setStartPage(i);
            stripper.setEndPage(i);
            documentText.append(stripper.getText(documentLoaded));
        }

        Document doc = new Document(file.getName(), documentText.toString());
        documentLoaded.close();
        return this.utils.executorForAnalyzing.submit(new docAnalyzingTask(doc, utils, GUIVersion));
    }
}
