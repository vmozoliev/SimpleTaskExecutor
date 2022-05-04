package com.belka.taskexecutor;

import com.belka.taskexecutor.exception.ExecutionTaskException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
class TaskExecutor<T, I> implements Supplier<T> {

    private final AbstractTask<T, I> task;

    public TaskExecutor(AbstractTask<T, I> task) {
        this.task = task;
    }

    @Override
    public T get() {
        task.setState(TaskState.IN_PROGRESS);

        T result;
        try {
            result = task.doTask();
        } catch (Exception e) {
            log.error("Error execute task id: " + task.getId());
            task.setState(TaskState.ERROR);
            task.setException(e);
            throw new ExecutionTaskException(e);
        }

        task.setResult(result);
        task.setState(TaskState.DONE_SUCCESSFULLY);
        return result;
    }
}
