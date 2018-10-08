package com.hazelcast.it;

import com.hazelcast.it.task.RemoteTask;

public interface TaskHandler {
    void handleTask(RemoteTask<Object> task);
}
