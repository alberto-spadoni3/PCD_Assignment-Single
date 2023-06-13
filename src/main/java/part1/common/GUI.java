package part1.common;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import org.apache.commons.lang3.SystemUtils;
import part1.threads.gui.InputListener;

public class GUI extends JFrame {
    private JButton startButton;
    private JButton stopButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton selectDirButton;

    private JLabel statusLabel;
    private JLabel selectedDirLabel;
    private JLabel docFoundLabel;
    private JLabel docAnalyzedLabel;
    private JLabel docWithWordLabel;

    private JTextField wordToSearch;

    private String selectedDir;
    private String selectedWord;
    
    private final int TEXT_SIZE = 16;
    private double computationDuration;

    private boolean wordChosen = false;
    private boolean directorySelected = false;

    private final ArrayList<InputListener> listeners = new ArrayList<>(1);

    public GUI(String title) {
        super(title);

        this.setUpFrame();
        this.setUpSelectionPanel();
        this.setUpResultsAndActionPanel();
        this.setUpStatusPanel();

        this.setUpActionsListener();

        this.pack();
    }

    private void setUpFrame() {
        this.setSize(1200, 800);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    private void setUpSelectionPanel() {
        JPanel baseSelectionPanel = new JPanel();
        baseSelectionPanel.setPreferredSize(new Dimension(350, 110));
        baseSelectionPanel.setLayout(new BoxLayout(baseSelectionPanel, BoxLayout.Y_AXIS));

        JPanel dirSelectionPanel = new JPanel();
        dirSelectionPanel.setBackground(new Color(0xE0E0E0));
        dirSelectionPanel.add(getLabelWithCustomTextSize("Select a directory", this.TEXT_SIZE));
        this.selectDirButton = this.getButtonWithName("Select");
        dirSelectionPanel.add(selectDirButton);
        this.selectedDirLabel = getLabelWithCustomTextSize("", this.TEXT_SIZE);
        dirSelectionPanel.add(selectedDirLabel);

        JPanel wordSelectionPanel = new JPanel();
        wordSelectionPanel.setBackground(new Color(0xE0E0E0));
        wordSelectionPanel.add(getLabelWithCustomTextSize("Word to search (press enter after typing)", this.TEXT_SIZE));
        this.wordToSearch = new JTextField(10);
        this.wordToSearch.setFont(new Font(null, Font.PLAIN, this.TEXT_SIZE));
        wordSelectionPanel.add(wordToSearch);

        baseSelectionPanel.add(dirSelectionPanel);
        baseSelectionPanel.add(wordSelectionPanel);
        baseSelectionPanel.setBackground(new Color(0xE0E0E0));
        this.add(baseSelectionPanel, BorderLayout.PAGE_START);
    }

    private void setUpResultsAndActionPanel() {
        JPanel resActionPanel = new JPanel();
        resActionPanel.setPreferredSize(new Dimension(450, 133));

        resActionPanel.add(this.setUpActionsPanel());
        resActionPanel.add(this.setUpResultsPanel());
        resActionPanel.setBackground(new Color(0xE0E0E0));

        this.add(resActionPanel, BorderLayout.CENTER);
    }

    private JPanel setUpActionsPanel() {
        this.startButton = this.getButtonWithName("Start");

        this.stopButton = this.getButtonWithName("Stop");
        this.stopButton.setEnabled(false);

        this.pauseButton = this.getButtonWithName("Pause");
        this.pauseButton.setEnabled(false);

        this.resumeButton = this.getButtonWithName("Resume");
        this.resumeButton.setEnabled(false);

        JPanel actionPanel = new JPanel();
        actionPanel.setBackground(new Color(0xE0E0E0));
        actionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));


        actionPanel.add(this.startButton);
        actionPanel.add(this.stopButton);
        actionPanel.add(this.pauseButton);
        actionPanel.add(this.resumeButton);

        return actionPanel;
    }

    private void setUpStatusPanel() {
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        statusPanel.setBackground(Color.LIGHT_GRAY);
        this.statusLabel = new JLabel("Ready"); // Default value for the status
        this.statusLabel.setFont(new Font(statusPanel.getFont().getName(), Font.BOLD, this.TEXT_SIZE));

        statusPanel.add(getLabelWithCustomTextSize("Status:", this.TEXT_SIZE));
        statusPanel.add(this.statusLabel);

        this.add(statusPanel, BorderLayout.PAGE_END);
    }

    private JPanel setUpResultsPanel() {
        this.docFoundLabel = new JLabel("0");
        this.docFoundLabel.setFont(new Font(null, Font.BOLD, this.TEXT_SIZE));

        this.docAnalyzedLabel = new JLabel("0");
        this.docAnalyzedLabel.setFont(new Font(null, Font.BOLD, this.TEXT_SIZE));

        this.docWithWordLabel = new JLabel("0");
        this.docWithWordLabel.setFont(new Font(null, Font.BOLD, this.TEXT_SIZE));

        JPanel resultsBasePanel = new JPanel();
        resultsBasePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
        labelsPanel.add(this.getLabelWithCustomTextSize("PDF documents found: ", this.TEXT_SIZE));
        labelsPanel.add(getLabelWithCustomTextSize("PDF documents analyzed: ", this.TEXT_SIZE));
        labelsPanel.add(getLabelWithCustomTextSize("PDF documents containing the word: ", this.TEXT_SIZE));

        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.add(docFoundLabel);
        resultsPanel.add(docAnalyzedLabel);
        resultsPanel.add(docWithWordLabel);

        resultsBasePanel.add(labelsPanel);
        resultsBasePanel.add(resultsPanel);

        return resultsBasePanel;
    }

    private JLabel getLabelWithCustomTextSize(String text, int textSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, textSize));
        return label;
    }

    private JButton getButtonWithName(String name) {
        JButton button = new JButton(name);
        button.setPreferredSize(new Dimension(100, 30));
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, this.TEXT_SIZE));
        return button;
    }

    private void setUpActionsListener() {
        this.selectDirButton.addActionListener(event -> {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (dirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                this.selectedDir = dirChooser.getSelectedFile().getAbsolutePath();

                String[] splittedPath = SystemUtils.IS_OS_WINDOWS ?
                        this.selectedDir.replace("\\", "/").split("/") :
                        this.selectedDir.split("/");

                String dirNameFromPath = splittedPath[splittedPath.length - 1];
                this.selectedDirLabel.setText(dirNameFromPath);
                this.directorySelected = true;
            }
        });

        this.wordToSearch.addActionListener(event -> {
            String text = this.wordToSearch.getText();
            if (text != null && !text.isBlank()) {
                this.selectedWord = this.wordToSearch.getText();
                this.wordToSearch.setBackground(new Color(0xccffcc));
                this.wordChosen = true;
            } else {
                this.wordToSearch.setBackground(new Color(0xffcccc));
                this.wordChosen = false;
            }
        });

        this.startButton.addActionListener(event -> {
            if (this.wordChosen && this.directorySelected) {
                this.startButton.setEnabled(false);
                this.stopButton.setEnabled(true);
                this.pauseButton.setEnabled(true);
                this.selectDirButton.setEnabled(false);
                this.resetResultLabels();
                this.statusLabel.setText("Running...");
                System.out.println(Thread.currentThread().getName());

                this.notifyStarted(new File(this.selectedDir), this.selectedWord);
            }
            else
                this.changeState("Can't start: missing inputs");
        });

        this.stopButton.addActionListener(event -> {
            this.startButton.setEnabled(true);
            this.stopButton.setEnabled(false);
            this.pauseButton.setEnabled(false);
            this.selectDirButton.setEnabled(true);
            this.changeState("Stopped after " + this.computationDuration/1000 + "s");

            this.notifyStopped();
        });

        this.pauseButton.addActionListener(event -> {
            this.pauseButton.setEnabled(false);
            this.stopButton.setEnabled(false);
            this.resumeButton.setEnabled(true);
            this.changeState("Paused");

            this.notifyPaused();
        });

        this.resumeButton.addActionListener(event -> {
            this.resumeButton.setEnabled(false);
            this.pauseButton.setEnabled(true);
            this.stopButton.setEnabled(true);
            this.changeState("Running...");

            this.notifyResumed();
        });
    }

    public void update(int docFound, int docAnalyzed, int docWithWord) {
        SwingUtilities.invokeLater(() -> {
            this.docFoundLabel.setText(String.valueOf(docFound));
            this.docAnalyzedLabel.setText(String.valueOf(docAnalyzed));
            this.docWithWordLabel.setText(String.valueOf(docWithWord));
        });
    }

    public void addListener(InputListener l) {
        this.listeners.add(l);
    }

    private void notifyStarted(File dir, String wordToSearch) {
        for (InputListener l: listeners)
            l.started(dir, wordToSearch);
    }

    private void notifyStopped() {
        for (InputListener l: listeners)
            l.stopped();
    }

    private void notifyPaused() {
        for (InputListener l: listeners)
            l.paused();
    }

    private void notifyResumed() {
        for (InputListener l: listeners)
            l.resumed();
    }

    public void done() {
        SwingUtilities.invokeLater(() -> {
            this.statusLabel.setText("Done in " + this.computationDuration/1000 + "s");

            this.startButton.setEnabled(true);
            this.stopButton.setEnabled(false);
            this.pauseButton.setEnabled(false);
            this.resumeButton.setEnabled(false);
            this.selectDirButton.setEnabled(true);
        });
    }

    private void resetResultLabels() {
        SwingUtilities.invokeLater(() -> {
            this.docFoundLabel.setText("0");
            this.docAnalyzedLabel.setText("0");
            this.docWithWordLabel.setText("0");
        });
    }

    public void changeState(String state) {
        SwingUtilities.invokeLater(() -> this.statusLabel.setText(state));
    }

    public void setDuration(int duration) {
        this.computationDuration = duration;
    }
}
