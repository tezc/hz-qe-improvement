package com.hazelcast.it;

import com.hazelcast.it.task.RemoteTask;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class Server extends Thread {

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

        System.out.println("Listening on : " + ip + ":" + port);

        accept();
        start();
    }

    private void accept() throws IOException {
        client = serverSocket.accept();

        System.out.println("Client connected");

        msgHandler = new MsgHandler(taskHandler, client.getInputStream(), client.getOutputStream());
    }

    public void shutdown() {
        stop = true;
        this.interrupt();
    }

    private void closeClient() {
        System.out.println("Client disconnected");

        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void close() {
        closeClient();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public <T> CompletableFuture<T> sendRequest(RemoteTask<T> task) throws IOException {
        return msgHandler.sendRequest(task);
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
            t.printStackTrace();
        }
        finally {
            close();
        }
    }
}
