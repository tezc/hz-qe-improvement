package com.hazelcast.it.msg;

public class Request extends Msg {

    public Request(long sequence, Object data)  {
        super(sequence, data);
    }
}
