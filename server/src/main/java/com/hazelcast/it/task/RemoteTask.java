package com.hazelcast.it.task;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.it.MsgHandler;
import com.hazelcast.it.msg.Request;

import java.io.Serializable;
import java.util.concurrent.Callable;

public abstract class RemoteTask<T> implements Serializable, Callable<Void>, HazelcastInstanceAware {

    protected transient HazelcastInstance instance;
    private transient MsgHandler msgHandler;
    private transient Request request;

    @Override
    public void setHazelcastInstance(HazelcastInstance instance) {
        this.instance = instance;
    }

    public void setMsgHandler(MsgHandler msgHandler) {
        this.msgHandler = msgHandler;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public abstract T execute();

    public Void call() {
        Object resp = null;
        try {
            resp = execute();
        }
        catch (Throwable t) {
            resp = t;
        }
        finally {
            try {
                msgHandler.sendResponse(request, resp);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return null;
    }
}
