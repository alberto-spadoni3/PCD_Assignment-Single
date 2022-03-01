package part1.threads.gui;

public class Main {
    public static void main(String[] args) {
        View view = new View("THREADS APPROACH");
        Controller controller = new Controller(view);
        view.addListener(controller);
        view.display();
    }
}
