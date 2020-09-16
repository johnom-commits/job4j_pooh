package ru.job4j.pooh;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Stream;

public class ClientSocket implements Closeable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ClientSocket(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            reader = createReader();
            writer = createWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientSocket(ServerSocket serverSocket) {
        try {
            socket = serverSocket.accept();
            reader = createReader();
            writer = createWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedReader createReader() throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private BufferedWriter createWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void writeLine(String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    public String readLine() throws IOException {
        return reader.readLine();
//        return reader.ready() ? reader.readLine() : null;
    }

    public Stream<String> lines() {
        return reader.lines();
    }

    @Override
    public void close() throws IOException {
        socket.close();
        reader.close();
        writer.close();
    }
}
