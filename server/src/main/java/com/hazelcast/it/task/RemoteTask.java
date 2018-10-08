package com.hazelcast.it.task;

import com.hazelcast.it.MsgHandler;
import com.hazelcast.it.msg.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Callable;

public abstract class RemoteTask<T> implements Serializable, Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(RemoteTask.class);

    private transient MsgHandler msgHandler;
    private transient Request request;

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
                logger.error("Error while sending {} ", resp);
            }
        }

        return null;
    }
}
