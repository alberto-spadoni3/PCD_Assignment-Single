package part1.threads.cli;

public class MyLatch {
    private int counter;

    public MyLatch(int counter) {
        this.counter = counter;
    }

    public synchronized void await() throws InterruptedException {
        while (this.counter > 0)
            wait();
    }

    public synchronized void countDown() {
        if (this.counter - 1 >= 0) {
            this.counter--;
            if (this.counter == 0)
                notifyAll();
        }
    }
}
