package part1.tasks.gui;

import part1.common.View;

public class Main {
    public static void main(String[] args) {
        View view = new View("TASKS APPROACH");
        Controller controller = new Controller(view);
        view.addListener(controller);
        view.display();
    }
}
