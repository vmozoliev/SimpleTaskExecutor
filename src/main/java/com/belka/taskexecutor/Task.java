package com.belka.taskexecutor;

import java.util.function.Supplier;

public class Task<T, I> extends AbstractTask<T, I> {

    private Supplier<T> supplier;

    public Task(I id, int priority, Supplier<T> supplier) {
        super(id, priority);
        this.supplier = supplier;
    }

    @Override
    protected T doTask() {
        return supplier.get();
    }
}
