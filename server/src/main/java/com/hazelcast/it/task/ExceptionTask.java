package com.hazelcast.it.task;

public class ExceptionTask extends RemoteTask<Object> {
    @Override
    public Object execute() {
        throw new RuntimeException("Throw exception");
    }
}
