package com.hazelcast.it;

import com.hazelcast.it.task.RemoteTask;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class Client extends Thread {
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
                System.out.println("Cannot connect to " + ip + ":" + port);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void connect() throws IOException {
        this.socket = new Socket(ip, port);
        System.out.println("Client connected");
        msgHandler = new MsgHandler(taskHandler, socket.getInputStream(), socket.getOutputStream());
    }

    public <T> CompletableFuture<T> sendRequest(RemoteTask<T> task) throws IOException, InterruptedException {
        return msgHandler.sendRequest(task);
    }

    public void shutdown() {
        stop = true;
        this.interrupt();
    }

    private void close() {
        System.out.println("TestClient disconnected");

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
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

            System.out.println("Stopping client...");
        } catch (Throwable t) {
            close();
            t.printStackTrace();
        }
    }
}
