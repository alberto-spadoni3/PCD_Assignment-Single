package part1.threads.cli;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBuffer<T> {
    private final LinkedList<T> buffer;

    private final Lock mutex;
    private final Condition notEmpty;
    private boolean closed;

    public ConcurrentBuffer() {
        this.buffer = new LinkedList<>();
        this.closed = false;
        this.mutex = new ReentrantLock();
        this.notEmpty = this.mutex.newCondition();
    }

    public void put(T item) {
        this.mutex.lock();
        try {
            this.buffer.addFirst(item);
            this.notEmpty.signalAll();
        } finally {
            this.mutex.unlock();
        }
    }

    public T get() throws BufferClosedException, InterruptedException {
        try {
            this.mutex.lock();

            while (this.isEmptyAndNotClosed())
                this.notEmpty.await();

            // Termination strategy
            if (this.isEmpty())
                throw new BufferClosedException();

            return this.buffer.removeFirst();
        } finally {
            this.mutex.unlock();
        }
    }

    public void closeBuffer() {
        try {
            this.mutex.lock();
            this.closed = true;
            this.notEmpty.signalAll();
        } finally {
            this.mutex.unlock();
        }
    }

    private boolean isEmpty() {
        return this.buffer.isEmpty();
    }

    private boolean isEmptyAndNotClosed() {
        return this.isEmpty() && !this.closed;
    }
}
