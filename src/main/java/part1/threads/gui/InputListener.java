package part1.threads.gui;

import java.io.File;

public interface InputListener {
    void started(File dir, String wordToSearch);

    void paused();

    void resumed();

    void stopped();
}
