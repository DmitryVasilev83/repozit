package com.geekbrains.nio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Set;



//Написать телнет терминал для ОС с базовыми операциями при помощи Nio на основе сервера, написанного на уроке
//
//        ls - список файлов в текущей директории
//        cat - вывод текста файла на экран
//        cd - перейти в директорию
//        touch - создать файл
//        mkdir - создать директорию



public class NioServer {

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ByteBuffer buf;

    public NioServer(int port) throws IOException {

        buf = ByteBuffer.allocate(5);
        serverChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);


        while (serverChannel.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            try {
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept();
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder msg = new StringBuilder();
        while (true) {
            int read = channel.read(buf);
            if (read == -1) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                msg.append((char) buf.get());
            }
            buf.clear();
        }


        processMessage(channel, msg.toString());
          //String response = "Hello " + msg + key.attachment();
          // channel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
    }

    private void processMessage(SocketChannel channel, String msg) throws IOException {
        //        ls - список файлов в текущей директории
//        cat - вывод текста файла на экран
//        cd - перейти в директорию
//        touch - создать файл
//        mkdir - создать директорию


        if (msg.startsWith("ls ")){

            String[] msgArr = msg.split(" ");
            String pathDir = msgArr[1].replaceAll("\n", "").replaceAll("\r", "");
            Path path = Paths.get(pathDir);

            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String fileString = file.toAbsolutePath().toString();
                        System.out.println(fileString);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch(IOException e){
                e.printStackTrace();
            }

        } else if (msg.startsWith("cat ")){

            String[] msgArr = msg.split(" ");
            String pathFile = msgArr[1].replaceAll("\n", "").replaceAll("\r", "");
            RandomAccessFile aFile = new RandomAccessFile(pathFile, "rw");
            FileChannel inChannel = aFile.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(48);
            int bytesRead = inChannel.read(buf);
            while (bytesRead != -1) {
                buf.flip();
                while(buf.hasRemaining()){
                    channel.write(buf);
                }
                buf.clear();
                bytesRead = inChannel.read(buf);
            }
            aFile.close();

        } else if (msg.startsWith("cd ")){

            String[] msgArr = msg.split(" ");
            String pathDir = msgArr[1].replaceAll("\n", "").replaceAll("\r", "");
            String response = "Current directory:" + pathDir + "\r\n";
            channel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));

        } else if (msg.startsWith("touch ")){

            String[] msgArr = msg.split(" ");
            String pathNewFile = msgArr[1].replaceAll("\n", "").replaceAll("\r", "");
            Path path = Paths.get(pathNewFile);
            try {
                Files.createFile(path);
            } catch(FileAlreadyExistsException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (msg.startsWith("mkdir ")){

            String[] msgArr = msg.split(" ");
            String pathNewDir = msgArr[1].replaceAll("\n", "").replaceAll("\r", "");
            Path path = Paths.get(pathNewDir);
            try {
                Path newDir = Files.createDirectory(path);
            } catch(FileAlreadyExistsException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }






    }

    private void handleAccept() throws IOException {
        System.out.println("Client accepted...");
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, "Hello world!");
    }

    public static void main(String[] args) throws IOException {
        new NioServer(8189);
    }

}
