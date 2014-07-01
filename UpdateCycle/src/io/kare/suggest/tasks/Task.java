package io.kare.suggest.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author arshsab
 * @since 06 2014
 */

public abstract class Task<I, O> {
    protected final ExecutorService exec;
    protected final List<Task<O, ?>> consumers = new ArrayList<>();
    private final String name;
    private volatile boolean shutdown = false;

    public Task(final int threads, String name) {
        this.exec = Executors.newFixedThreadPool(threads);
        this.name = name;
    }

    private void input(I i) {
        exec.submit(() -> {
            try {
                consume(i);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
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
        startTask();

        consumers.stream().forEach(t -> t.startChain());
    }

    public void awaitShutdown() throws InterruptedException {
        while (!shutdown)
            wait();
    }

    public void shutdown() {
        exec.shutdown();

        while (!exec.isTerminated()) {
            try {
                exec.awaitTermination(1000, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Could not properly shutdown task: " + toString());
            }
        }

        for (Task t : consumers) {
            t.shutdown();
        }

        this.shutdown = true;
        notifyAll();
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public String toString() {
        return "(Task: " + name + ")";
    }
}
