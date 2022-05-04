package com.belka.taskexecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class TaskManager<I> {

    private PriorityBlockingQueue<AbstractTask<?, I>> taskQueue = new PriorityBlockingQueue<>();
    private Map<I, AbstractTask<?, I>> uidTaskMap = new HashMap<>();

    private ExecutorService taskManagerExecutorService;
    private ExecutorService executorService;

    private volatile boolean isRunning;

    public TaskManager(ExecutorService executorService) {
        this.taskManagerExecutorService = Executors.newSingleThreadExecutor();
        this.executorService = executorService;
    }

    public TaskManager(int parallelism) {
        this(Executors.newWorkStealingPool(parallelism));
    }

    public I addTask(AbstractTask<?, I> task) {

        if (!task.getSate().equals(TaskState.NEW)) {
            throw new IllegalArgumentException();
        }

        taskQueue.put(task);
        uidTaskMap.put(task.getId(), task);

        task.setState(TaskState.APPLIED);

        return task.getId();
    }

    public AbstractTask<?, I> getTask(I id) {
        return uidTaskMap.get(id);
    }

    public boolean removeTask(I id) {
        AbstractTask<?, I> task = uidTaskMap.remove(id);
        return taskQueue.remove(task);
    }

    public <T> void run() {
        this.isRunning = true;
        taskManagerExecutorService.execute(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                AbstractTask<T, I> task;
                try {
                    task = (AbstractTask<T, I>) taskQueue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                CompletableFuture<T> future = CompletableFuture.supplyAsync(new TaskExecutor<>(task), executorService);
                task.setFuture(future);
            }
        });
    }

    public void stop() {
        isRunning = false;
    }

}
