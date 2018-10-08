package com.hazelcast.it;

import com.hazelcast.it.task.RemoteTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class Client extends Thread {
    private final Logger logger = LoggerFactory.getLogger(Client.class);

    private volatile boolean stop;

    private Socket socket;
    private String ip;
    private int port;

    private volatile MsgHandler msgHandler;
    private final TaskHandler taskHandler;

    public Client(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    public void connect(String ip, int port) {
        this.ip = ip;
        this.port = port;

        while (true) {
            try {
                connect();
                start();

                return;

            } catch (IOException e) {
                logger.warn("Cannot connect to " + ip + ":" + port);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    logger.warn("Interrupted .. ", e1);
                }
            }
        }
    }

    public void shutdown() {
        stop = true;
        this.interrupt();
        try {
            this.join();
        } catch (InterruptedException e) {
            logger.warn("Unexpected interrupt :" + e);
        }
    }

    private void connect() throws IOException {
        socket = new Socket(ip, port);
        logger.info("Client connected to " + ip + ":" + port);
        msgHandler = new MsgHandler(taskHandler, socket.getInputStream(), socket.getOutputStream());
    }

    private void close() {
        logger.info("Client shutting down..");

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn("Unexpected error " + e);
            }
        }
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                try {
                    msgHandler.read();
                }
                catch (IOException e) {
                    close();
                    Thread.sleep(3000);
                    connect();
                }
            }

            logger.info("Stopping client...");
        }
        catch (Throwable t) {
            close();
            logger.warn("Shutting down client thread : " + t);
        }
    }

    public <T> CompletableFuture<T> sendRequest(RemoteTask<T> task) throws IOException {
        return msgHandler.sendRequest(task);
    }
}
