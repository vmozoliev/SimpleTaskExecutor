package com.belka.taskexecutor;

import com.belka.taskexecutor.exception.ExecutionTaskException;
import com.belka.taskexecutor.exception.NotCompletedTaskException;

import java.util.concurrent.*;

import static com.belka.taskexecutor.TaskState.*;


public abstract class AbstractTask<T, I> implements Comparable<AbstractTask<T, I>> {
    private final I id;
    private volatile T result;
    private volatile TaskState state = NEW;
    private volatile Exception exception;
    private volatile CompletableFuture<T> future;

    private final int priority;

    protected AbstractTask(I id, int priority) {
        this.priority = priority;
        this.id = id;
    }

    protected abstract T doTask();

    public I getId() {
        return id;
    }

    public AbstractTask<T, I> reuse() {
        this.setState(NEW);
        this.setException(null);
        this.setFuture(null);
        this.setResult(null);
        return this;
    }

    public boolean isDone() {
        return state.equals(DONE_SUCCESSFULLY) || state.equals(ERROR) || state.equals(TERMINATED);
    }

    public boolean isDoneSuccessfully() {
        return state.equals(DONE_SUCCESSFULLY);
    }

    public TaskState getSate() {
        return state;
    }

    void setResult(T result) {
        this.result = result;
    }

    void setState(TaskState state) {
        this.state = state;
    }

    void setFuture(CompletableFuture<T> future) {
        this.future = future;
    }

    public T getResult() throws NotCompletedTaskException {
        if (isDoneSuccessfully()) {
            return result;
        }
        throw new NotCompletedTaskException();
    }

    public void cancel() {
        waitFuture();
        future.cancel(false);
    }

    public T join() {
        waitFuture();
        return this.future.join();
    }

    public T getResult(long timeout, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        if (isDoneSuccessfully()) {
            return result;
        }
        if (state == ERROR || state == TERMINATED) {
            throw new ExecutionTaskException(exception);
        }

        waitFuture();

        try {
            result = future.get(timeout, timeUnit);
        } catch (InterruptedException e) {
            state = ERROR;
            throw e;
        } catch (ExecutionException e) {
            state = ERROR;
            setException(e);
            throw new ExecutionTaskException(e);
        } catch (TimeoutException e) {
            throw e;
        }

        return result;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public int compareTo(AbstractTask obj) {
        return obj.priority - this.priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractTask)) {
            return false;
        }
        return id.equals(((AbstractTask<?,?>)obj).getId());
    }

    private void waitFuture() {
        while (future == null) Thread.yield();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
