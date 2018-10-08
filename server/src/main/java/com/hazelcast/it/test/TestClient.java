package com.hazelcast.it.test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.it.Client;
import com.hazelcast.it.task.ExceptionTask;
import com.hazelcast.it.task.RemoteTask;
import com.hazelcast.it.task.SimplePutTask;
import com.hazelcast.it.TaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestClient implements TaskHandler, Serializable
{
    private final Logger logger = LoggerFactory.getLogger(TestClient.class);

    private final HazelcastInstance instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public TestClient() {
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress("172.17.0.1:5701");
        instance = HazelcastClient.newHazelcastClient(config);
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
                logger.error("Exception task threw exception : ", th);
                return null;
            });

            /*
             * To serialize anonymous non static classes, we must declare wrapper
             * class as Serializable, also receiver side should have wrapper class in its classpath
             *
             * For BlackDuck, probe must have test classes as dependency, also test class' must be declared serializable
             *
             *
            CompletableFuture<Object> future4 = client.sendRequest(new RemoteTask<Object>() {
                @Override
                public Object execute() {
                    return "hops";
                }
            });


            System.out.println(future4.get());
            */

        }
        catch (Exception e) {
            logger.error("Exception : ", e);
        }
    }

    @Override
    public void handleTask(RemoteTask<Object> task) {
        if (task instanceof HazelcastInstanceAware) {
            ((HazelcastInstanceAware)task).setHazelcastInstance(instance);
        }

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