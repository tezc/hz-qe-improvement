package com.hazelcast.it;

import com.hazelcast.it.msg.Request;
import com.hazelcast.it.msg.Response;
import com.hazelcast.it.task.RemoteTask;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MsgHandler {
    private final Object lock = new Object();

    private final Map<Long, CompletableFuture> awatingResponses = new HashMap<>();

    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final TaskHandler taskHandler;


    private int requestID;

    public MsgHandler(TaskHandler taskHandler, InputStream in, OutputStream out) throws IOException {
        this.taskHandler = taskHandler;
        this.out = new ObjectOutputStream(out);
        this.out.flush();
        this.in = new ObjectInputStream(in);
    }

    public void read() throws IOException, ClassNotFoundException {
        Object o = in.readObject();
        if (o instanceof Request) {
            handleRequest((Request) o);
        }
        else {
            handleResponse((Response) o);
        }
    }

    public <T> CompletableFuture<T> sendRequest(RemoteTask<T> task) throws IOException {
        synchronized (lock) {
            Request request = new Request(requestID++, task);
            CompletableFuture<T> future = new CompletableFuture<>();
            awatingResponses.put(request.getSequence(), future);
            out.writeObject(request);
            out.reset();

            return future;
        }
    }

    public void sendResponse(Request request, Object o) throws IOException {
        synchronized (lock) {
            Response response = new Response(request.getSequence(), o);
            out.writeObject(response);
            out.reset();
        }
    }

    @SuppressWarnings("unchecked")
    private void handleRequest(Request request) {
        RemoteTask<Object> task = (RemoteTask<Object>) request.getData();
        task.setRequest(request);
        task.setMsgHandler(this);

        taskHandler.handleTask(task);
    }

    @SuppressWarnings("unchecked")
    private void handleResponse(Response response) {
        synchronized (lock) {
            CompletableFuture<Object> future = awatingResponses.get(response.getSequence());
            if (response.getData() instanceof Throwable) {
                future.completeExceptionally((Throwable) response.getData());
            } else {
                future.complete(response.getData());
            }
        }
    }
}
