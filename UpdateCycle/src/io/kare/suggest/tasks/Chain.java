package io.kare.suggest.tasks;

/**
 * @author arshsab
 * @since 07 2014
 */

public class Chain {
    private final Task[] chain;

    @SuppressWarnings("unchecked")
    public Chain(Task[] tasks) {
        for (int i = 0; i < tasks.length - 1; i++) {
            tasks[i].addConsumer(tasks[i + 1]);
        }

        this.chain = tasks;
    }

    public void start() {
        chain[0].startChain();
    }

    public void shutdown() throws InterruptedException {
        chain[0].shutdown();
    }
}
