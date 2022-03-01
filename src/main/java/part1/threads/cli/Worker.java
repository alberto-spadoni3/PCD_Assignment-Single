package part1.threads.cli;

public class Worker extends Thread {
    public Worker(String name) {
        super(name);
    }

    public void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}
