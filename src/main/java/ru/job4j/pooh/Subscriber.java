package ru.job4j.pooh;

import java.io.IOException;
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
        String line;
        while (isRunnable) {
            try (var socket = new ClientSocket("127.0.0.1", PORT)) {
                socket.writeLine(query);
                Thread.sleep(100);
                if ((line = socket.readLine()) != null) {
                    list.add(line);
                }
                Thread.sleep(5000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        isRunnable = false;
    }
}