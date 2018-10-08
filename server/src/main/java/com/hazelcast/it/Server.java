package com.hazelcast.it;

import com.hazelcast.it.task.RemoteTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class Server extends Thread {

    private final Logger logger = LoggerFactory.getLogger(Client.class);
    private final TaskHandler taskHandler;

    private volatile boolean stop;
    private ServerSocket serverSocket;
    private Socket client;

    private volatile MsgHandler msgHandler;

    public Server(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    public void accept(String ip, int port) throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(ip, port));

        logger.info("Listening on : " + ip + ":" + port);

        accept();
        start();
    }

    private void accept() throws IOException {
        client = serverSocket.accept();

        logger.info("Client connected from : " + client.getRemoteSocketAddress());

        msgHandler = new MsgHandler(taskHandler, client.getInputStream(), client.getOutputStream());
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

    private void closeClient() {
        logger.info("Shutting down the client ...");

        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                logger.warn("Unexpected exception : " + e);
            }
        }
    }

    private void close() {
        closeClient();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.warn("Unexpected exception : " + e);
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
                    closeClient();
                    accept();
                }
            }
        } catch (Throwable t) {
            logger.warn("Server shutting down.. " + t);
        }
        finally {
            close();
        }
    }

    public <T> CompletableFuture<T> sendRequest(RemoteTask<T> task) throws IOException {
        return msgHandler.sendRequest(task);
    }
}
