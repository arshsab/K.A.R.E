package io.kare.suggest.tasks;

/**
 * @author arshsab
 * @since 06 2014
 */

public abstract class Producer<O> extends Task<Void, O> {

    public Producer(String name) {
        super(1, name);
    }

    @Override
    protected void consume(Void aVoid) {
        // NO-OP
    }

    @Override
    public void startTask() {
        exec.submit(this::produce);
    }

    protected abstract void produce();
}
