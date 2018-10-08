package com.hazelcast.it.task;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;

public class SimplePutTask extends RemoteTask<String> implements HazelcastInstanceAware
{
    private transient HazelcastInstance instance;

    private final String key;
    private final String value;

    public SimplePutTask(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance instance) {
        this.instance = instance;
    }

    @Override
    public String execute() {
        IMap<String, String> map = instance.getMap("map");
        return map.put("key", "value");
    }
}