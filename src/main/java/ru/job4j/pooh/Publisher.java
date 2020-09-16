package ru.job4j.pooh;

import java.io.IOException;

public class Publisher {
    private final int PORT = 5672;

    public static void main(String[] args) {
       Publisher publisher = new Publisher();
       publisher.exec("POST /queue\n {\n \"queue\" : \"weather\",\n \"text\" : \"temperature +18 C\" \n}");
       publisher.exec("POST /topic\n {\n \"topic\" : \"weather\",\n \"text\" : \"temperature +18 C\" \n}");
    }

    public void exec(String request) {
        try (var socket = new ClientSocket("127.0.0.1", PORT)) {
            socket.writeLine(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
