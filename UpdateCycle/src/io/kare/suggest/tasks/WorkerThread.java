package io.kare.suggest.tasks;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author arshsab
 * @since 07 2014
 */

public class WorkerThread extends Thread {
    private final ArrayBlockingQueue<Runnable> queue;

    public WorkerThread(ArrayBlockingQueue<Runnable> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Runnable r = queue.take();

                if (r instanceof FinishedRunnable) {
                    break;
                }

                r.run();
            }
        } catch (InterruptedException ignored) {}
    }
}
