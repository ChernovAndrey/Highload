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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {

    private static  String pathToConfig = "/home/andrey/httpd.config";
    private static  int PORT = 80;
    private static int MaxThreads=50;

    private static void parseConfig() throws IOException {
        FileInputStream fstream = new FileInputStream(pathToConfig);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;

//Read File Line By Line
        while ((strLine = br.readLine()) != null) {
            // Print the content on the console
            final String[] maps = strLine.split(":");
            if(maps[0].equals("Listen")){
                PORT = Integer.parseInt(maps[1]);
            }
            if(maps[0].equals("threads_max")){
                System.out.println(Integer.parseInt(maps[1]));
               MaxThreads = Integer.parseInt(maps[1]);
            }
            if(maps[0].equals("document_root")){
                ServeClient.root = maps[1];
            }
            System.out.println(strLine);
        }
        fstream.close();
        br.close();
    }


    public static void main(String[] args) throws IOException {
        parseConfig();
        ServerSocket s = new ServerSocket(PORT);


  //      final ExecutorService threadPool = Executors.newFixedThreadPool(MaxThreads);
       final ThreadPool threadPool= new ThreadPool(MaxThreads);
        ServeClient.initTypeFiles();

        while(true) {
            Socket socket = s.accept();
            ServeClient sc = new ServeClient(socket);
            threadPool.execute(sc);
        }
    }
}
