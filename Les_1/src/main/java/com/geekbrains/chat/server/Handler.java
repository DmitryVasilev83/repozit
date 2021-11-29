package com.geekbrains.chat.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Handler implements Runnable {

    private boolean running;
    private final byte[] buf;
    private final InputStream is;
    private final OutputStream os;
    private final Socket socket;

    public Handler(Socket socket) throws IOException {
        running = true;
        buf = new byte[8192];
        this.socket = socket;
        is = socket.getInputStream();
        os = socket.getOutputStream();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            String fileName = "";
            while (running) {
                // вкрутить логику с получением файла от клиента
                int read = is.read(buf);
                String message = new String(buf, 0, read);

                                       //  Определяем имя файла, который надо создать
                if (message.startsWith("filePath/")){
                    String[] fileNameArr = message.split("/");
                    fileName = fileNameArr[fileNameArr.length-1];
                    File file = new File("files_server/" + fileName);

                }

                copyToFile(buf, fileName);         // записываем получаемые данные в файл



                if (message.equals("quit")) {
                    os.write("Client disconnected\n".getBytes(StandardCharsets.UTF_8));
                    close();
                    break;
                }
                // System.out.println("Received: " + message);
                // os.write((message + "\n").getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() throws IOException {
        os.close();
        is.close();
        socket.close();
    }

    private void copyToFile(byte[] msg, String fileName){         //  Запись полученных сервером байтов в файл

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
