package part1.reactive.cli;

public abstract class ActiveComponent {
    public void log(String message) {
        String currentThreadName = Thread.currentThread().getName();
        System.out.println("[" + currentThreadName + "] " + message);
    }
}
