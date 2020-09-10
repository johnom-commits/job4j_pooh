package ru.job4j.pooh;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Subscriber {
    private final int PORT = 5672;
    private final ConcurrentLinkedQueue<String> list = new ConcurrentLinkedQueue<>();
    private volatile boolean isRunnable = true;
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        Subscriber subscriber = new Subscriber();
        subscriber.exec();
    }

    private void exec() {
        executor.execute(() -> task("GET /queue/weather"));
        executor.execute(() -> task("GET /topic/weather"));
    }

    public void task(String query) {
        while (isRunnable) {
            try (Socket socket = new Socket("127.0.0.1", PORT);
                 var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                writer.write(query);
                writer.newLine();
                writer.flush();
                Thread.sleep(100);
                if (reader.ready()) {
                    list.add(reader.readLine());
                }
                Thread.sleep(5000);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        isRunnable = false;
    }
}