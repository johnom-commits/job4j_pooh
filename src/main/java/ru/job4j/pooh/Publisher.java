package ru.job4j.pooh;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Publisher {
    private final int PORT = 5672;

    public static void main(String[] args) {
       Publisher publisher = new Publisher();
       publisher.exec("POST /queue\n {\n \"queue\" : \"weather\",\n \"text\" : \"temperature +18 C\" \n}");
       publisher.exec("POST /topic\n {\n \"topic\" : \"weather\",\n \"text\" : \"temperature +18 C\" \n}");
    }

    public void exec(String request) {
        try (Socket socket = new Socket("127.0.0.1", PORT);
             var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        ) {
            writer.write(request);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
