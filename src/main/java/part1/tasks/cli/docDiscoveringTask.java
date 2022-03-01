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

    public docDiscoveringTask(File rootDirectory, Utils utils) {
        this.rootDirectory = rootDirectory;
        this.utils = utils;
    }

    @Override
    public List<Future<Future<Void>>> call() {
        List<Future<Future<Void>>> docToLoadTasks = new ArrayList<>();
        this.discover(rootDirectory, docToLoadTasks);
        System.out.println("Found " + this.utils.documentsCounter.getDocumentsFound() + " documents");
        return docToLoadTasks;
    }

    private void discover(File rootDirectory, List<Future<Future<Void>>> docToLoadTasks) {
        for (File file : Objects.requireNonNull(rootDirectory.listFiles())) {
            if (file.isDirectory())
                this.discover(file, docToLoadTasks);
            else if (file.getName().toLowerCase().endsWith(".pdf")) {
                docToLoadTasks.add(this.utils.executorForLoading.submit(new docLoadingTask(file, utils)));
                this.utils.documentsCounter.incrementDocumentsFound();
            }
        }
    }
}
