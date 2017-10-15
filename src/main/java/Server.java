/**
 * Created by andrey on 04.10.17.
 */
//: c15:Server.java
// Очень простой сервер, который просто отсылает
// назад все, что посылает клиент.
// {RunByHand}

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {

    public static final int PORT = 8080;


    public static void main(String[] args) throws IOException {
        final ExecutorService threadPool = Executors.newFixedThreadPool(100);

        ServeClient.initTypeFiles();
        final ServerSocket s = new ServerSocket(PORT);
        System.out.println("Started: " + s);
        try {
            // Блокирует до тех пор, пока не возникнет соединение:
            while (true) {
                 final Socket socket = s.accept();
                threadPool.submit(() -> {
                        new ServeClient(socket).execute();
                });

            }
        }finally {
            s.close();
        }

    }
}
