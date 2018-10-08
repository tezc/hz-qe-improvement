package com.hazelcast.it.test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.it.task.RemoteTask;
import com.hazelcast.it.Server;
import com.hazelcast.it.TaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestServer  implements TaskHandler {
    private final Logger logger = LoggerFactory.getLogger(TestClient.class);

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

    public static void main(String[] args) {

        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.getInterfaces().addInterface("127.0.0.1").setEnabled(true);
        networkConfig.setPortAutoIncrement(false);

        JoinConfig join = networkConfig.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getAwsConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true);
        join.getTcpIpConfig().addMember("127.0.0.1:5701");

        HazelcastInstance member = Hazelcast.newHazelcastInstance(config);

        TestServer testServer = new TestServer();
        testServer.start();
    }
}