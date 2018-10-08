package com.hazelcast.it.msg;

import java.io.*;

public abstract class Msg implements Serializable
{
    private final long sequence;
    private final Object data;

    protected Msg(long sequence, Object data)
    {
        this.sequence = sequence;
        this.data = data;
    }

    public long getSequence() {
        return sequence;
    }

    public Object getData() {
        return data;
    }
}