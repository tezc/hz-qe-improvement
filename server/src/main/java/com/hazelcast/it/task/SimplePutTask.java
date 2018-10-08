package com.hazelcast.it.task;

import com.hazelcast.core.IMap;

public class SimplePutTask extends RemoteTask<String>
{
    private final String key;
    private final String value;

    public SimplePutTask(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String execute() {
        IMap<String, String> map = instance.getMap("map");
        return map.put("key", "value");
    }
}