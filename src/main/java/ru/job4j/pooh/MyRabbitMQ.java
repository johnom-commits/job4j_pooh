package ru.job4j.pooh;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class MyRabbitMQ {
    private final int PORT = 5672;
    private volatile boolean isRunnable = true;
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ConcurrentHashMap<String, String> queue = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> topic = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        MyRabbitMQ myRabbitMQ = new MyRabbitMQ();
        myRabbitMQ.exec();
    }

    public void exec() {
        try (var server = new ServerSocket(PORT)) {
            while (isRunnable) {
                try (
                        Socket socket = server.accept();
                        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                ) {
                    String request = reader.readLine();
                    Map<String, String> map = parseRequest(request);
                    Objects.requireNonNull(map);

                    if (map.get("kind").equals("POST")) {
                        String json = getJson(reader);
                        if (map.get("queue").equals("queue")) {
                            executor.submit(getTaskForPost(json, queue, "queue"));
                        } else {
                            executor.submit(getTaskForPost(json, topic, "topic"));
                        }
                    } else {
                        // обрабатываем GET
                        Future<String> future;
                        if (map.get("queue").equals("queue")) {
                            future = executor.submit(getTaskForGet(request, queue));
                        } else {
                            future = executor.submit(getTaskForGet(request, topic));
                        }
                        String answer = future.get();
                        if (answer != null) {
                            writer.write(answer);
                            writer.newLine();
                            writer.flush();
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Callable<String> getTaskForGet(String request, ConcurrentHashMap<String, String> map) {
        return () -> {
            String header = getHeader(request);
            String answer = map.get(header);
            map.remove(header);
            return answer;
        };
    }

    private String getHeader(String request) {
        // Вытаскиваем "weather" из запроса "GET /queue/weather"
        int index = request.lastIndexOf("/");
        if (index == -1) {
            return null;
        }
        return request.substring(index + 1, request.length());
    }

    private Runnable getTaskForPost(String json, ConcurrentHashMap<String, String> map, String name) {
        return () -> {
            Object obj = null;
            try {
                obj = new JSONParser().parse(json);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            JSONObject jo = (JSONObject) obj;
            if (jo != null) {
                String header = (String) jo.get(name);
                map.put(header, json);
            }
        };
    }

    private String getJson(BufferedReader reader) {
        Stream<String> body = reader.lines();
        var builder = new StringBuilder();
        body.forEach(builder::append);
        return builder.toString();
    }

    private Map<String, String> parseRequest(String request) {
        int index = request.indexOf("/");
        if (index == -1) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        map.put("kind", request.substring(0, index - 1));
        map.put("queue", request.substring(index + 1, index + 6));
        return map;
    }

    public void shutdown() {
        isRunnable = false;
        executor.shutdown();
    }
}
