package com.belka.taskexecutor.exception;

public class ExecutionTaskException extends RuntimeException {
    public ExecutionTaskException(Exception ex) {
        super(ex);
    }
}
