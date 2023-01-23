package part1.eventloop.gui;

import part1.common.View;

public class Main {
    public static void main(String[] args) {
        View view = new View("EVENT-LOOP APPROACH");
        Controller controller = new Controller(view);
        view.addListener(controller);
        view.display();
    }
}
