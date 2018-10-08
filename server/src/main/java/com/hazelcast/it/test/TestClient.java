package com.hazelcast.it.test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.it.Client;
import com.hazelcast.it.task.ExceptionTask;
import com.hazelcast.it.task.RemoteTask;
import com.hazelcast.it.task.SimplePutTask;
import com.hazelcast.it.TaskHandler;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestClient implements TaskHandler, Serializable
{
    private final HazelcastInstance instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public TestClient() {
        instance = HazelcastClient.newHazelcastClient();
    }

    public void start() {
        try {
            Client client = new Client(this);
            client.connect("127.0.0.1", 9090);

            //Non-blocking
            CompletableFuture<String> future = client.sendRequest(new SimplePutTask("key", "value1"));
            future.thenAccept(System.out::println);

            //Blocking
            CompletableFuture<String> future2 = client.sendRequest(new SimplePutTask("key", "value2"));
            System.out.println(future2.get());

            //Exception
            CompletableFuture<Object> future3 = client.sendRequest(new ExceptionTask());
            future3.thenAccept(System.out::println);
            future3.exceptionally(th -> {
                System.out.println("Exception task threw exception : ");
                th.printStackTrace();
                return null;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTask(RemoteTask<Object> task) {
        task.setHazelcastInstance(instance);
        executor.submit(task);
    }

    public static void main(String[] args)
    {
        try {
            new TestClient().start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}