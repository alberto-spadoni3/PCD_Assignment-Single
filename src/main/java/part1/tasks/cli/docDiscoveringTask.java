package part1.tasks.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class docDiscoveringTask implements Callable<List<Future<Future<Void>>>> {
    private final File rootDirectory;
    private final Utils utils;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public docDiscoveringTask(File rootDirectory, Utils utils, boolean GUIVersion) {
        this.rootDirectory = rootDirectory;
        this.utils = utils;
        this.GUIVersion = GUIVersion;
    }


    @Override
    public List<Future<Future<Void>>> call() {
        List<Future<Future<Void>>> docToLoadTasks = new ArrayList<>();
        this.discover(rootDirectory, docToLoadTasks);
        System.out.println("Found " + this.utils.documentsCounter.getDocumentsFound() + " documents");
        return docToLoadTasks;
    }

    private void discover(File rootDirectory, List<Future<Future<Void>>> docToLoadTasks) {
        if (this.GUIVersion) {
            for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
                if (this.utils.terminationFlag.canProceed()) {
                    if (file.isDirectory())
                        this.discover(file, docToLoadTasks);
                    else if (file.getName().toLowerCase().endsWith(".pdf")) {
                        docToLoadTasks.add(this.utils.executorForLoading.submit(new docLoadingTask(file, utils, true)));
                        this.utils.documentsCounter.incrementDocumentsFound();
                    }
                } else if (this.utils.terminationFlag.isPaused())
                    this.utils.terminationFlag.waitToBeResumed();
                else { // the stop button is being pressed
                    break;
                }
            }
        }
        else {
            for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
                if (file.isDirectory())
                    this.discover(file, docToLoadTasks);
                else if (file.getName().toLowerCase().endsWith(".pdf")) {
                    docToLoadTasks.add(this.utils.executorForLoading.submit(new docLoadingTask(file, utils, false)));
                    this.utils.documentsCounter.incrementDocumentsFound();
                }
            }
        }
    }
}
