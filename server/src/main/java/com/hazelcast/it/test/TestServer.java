package com.hazelcast.it.test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.it.task.RemoteTask;
import com.hazelcast.it.Server;
import com.hazelcast.it.TaskHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestServer  implements TaskHandler {

    private final HazelcastInstance instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public TestServer() {
        instance = HazelcastClient.newHazelcastClient();
    }

    public void start() {
        try {
            Server server = new Server(this);
            server.accept("127.0.0.1", 9090);

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

    public static void main(String[] args) {

        HazelcastInstance member = Hazelcast.newHazelcastInstance();

        TestServer testServer = new TestServer();
        testServer.start();
    }
}