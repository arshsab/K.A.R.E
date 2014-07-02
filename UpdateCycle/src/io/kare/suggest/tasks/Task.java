package io.kare.suggest.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author arshsab
 * @since 06 2014
 */

public abstract class Task<I, O> {
    private final WorkerThread[] threads;
    protected final ArrayBlockingQueue<Runnable> queue;
    protected final List<Task<O, ?>> consumers = new ArrayList<>();
    private final String name;

    private volatile boolean shutdown = false;

    public Task(int threads, String name) {
        this(threads, 10_000, name);
    }

    public Task(final int threads, int queueSize, String name) {
        this.queue = new ArrayBlockingQueue<>(queueSize);
        this.threads = new WorkerThread[threads];

        for (int i = 0; i < this.threads.length; i++) {
            this.threads[i] = new WorkerThread(queue);
        }

        this.name = name;
    }

    private void input(I i) {
        if (shutdown) {
            return;
        }

        try {
            queue.put(() -> {
                try {
                    consume(i);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void consume(I i);

    protected void output(O o) {
        consumers.stream().forEach(t -> t.input(o));
    }

    public void addConsumer(Task<O, ?> consumer) {
        consumers.add(consumer);
    }

    public void startTask() {
        // NO-OP
    }

    public void startChain() {
        for (Thread t : threads) {
            t.start();
        }

        startTask();

        consumers.stream().forEach(t -> t.startChain());
    }

    public void shutdown() throws InterruptedException {
        this.shutdown = true;

        for (int i = 0; i < threads.length; i++) {
            queue.put(new FinishedRunnable());
        }

        for (Thread t : threads) {
            t.join();
        }

        for (Task t : consumers) {
            t.shutdown();
        }
    }

    public int getQueueSize() {
        return queue.size();
    }

    public String toString() {
        return "(Task: " + name + ")";
    }
}
