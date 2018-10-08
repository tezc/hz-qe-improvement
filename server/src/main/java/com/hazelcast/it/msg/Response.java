package com.hazelcast.it.msg;

public class Response extends Msg {

    public Response(long sequence, Object obj) {
        super(sequence, obj);
    }
}
