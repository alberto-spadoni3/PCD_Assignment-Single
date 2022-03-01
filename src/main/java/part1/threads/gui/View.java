package part1.threads.gui;

import javax.swing.*;

public class View {
    public final GUI viewFrame;

    public View(String title) {
        this.viewFrame = new GUI(title);
    }

    public void addListener(InputListener l) {
        this.viewFrame.addListener(l);
    }

    public void update(int docFound, int docAnalyzed, int docWithWord) {
        this.viewFrame.update(docFound, docAnalyzed, docWithWord);
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            this.viewFrame.setVisible(true);
        });
    }

    public void computationDone() {
        this.viewFrame.done();
    }

    public void changeState(String state) {
        this.viewFrame.changeState(state);
    }

    public void setComputationDuration(long duration) {
        this.viewFrame.setDuration((int) duration);
    }
}
