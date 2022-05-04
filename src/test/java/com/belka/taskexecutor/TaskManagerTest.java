package com.belka.taskexecutor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;


class TaskManagerTest {

    private TaskManager<String> taskManager = new TaskManager<>(8);

    @Test
    void addTask() throws InterruptedException, TimeoutException {

        taskManager.run();

        List<String> tasks = new LinkedList<>();
        for (int i = 0; i < 100000; i++) {
            int finalI = i;
            String uid = taskManager.addTask(new Task<Double, String>("" + i,0, () -> {
                return Math.pow(finalI, 25);
            }));
            tasks.add(uid);
        }

        for (String task : tasks) {
            Double result = (Double) taskManager.getTask(task).join();
            System.out.println(result);
            Assertions.assertNotNull(result);
            taskManager.removeTask(task);
        }

        taskManager.stop();

    }
}