import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by andrey on 15.10.17.
 */
public class ServeClient {
    private static final String root = "//home/andrey/IdeaProjects/HighloadMaven";
    private static final String indexFileName = "index.html";


    Socket socket;
    BufferedReader in;
    OutputStream raw;
    Writer out;
    String[] tokens;
    boolean slashAfterFileName = false;
    boolean flagIndex = false;
    static HashMap<String, String> typeFiles;


    private File getFile(int posQuestion, String fileName) {
        fileName=root+fileName;
        System.out.println(fileName);
        if (posQuestion == -1) {
            return new File( fileName);
                   // fileName.substring(1, fileName.length()));
        } else {
            return new File(fileName);
                   // fileName.substring(1, root.length()+posQuestion));
        }
    }

    private void Response() throws IOException {
        final String method = tokens[0];
        if (method.toUpperCase().equals("GET") || (method.toUpperCase().equals("HEAD"))) {
            String fileName = URLDecoder.decode(tokens[1], "UTF-8");

            final int posLastDot = fileName.lastIndexOf('.');
            if (fileName.endsWith("/")) {
                if (posLastDot != -1) { //соедржит точку
                    slashAfterFileName = true;
                } else {
                    flagIndex = true;
                    fileName += indexFileName;
                }
            }

            final String version = tokens[2];
            final String contentType = typeFiles.get(fileName.substring(posLastDot + 1));
            final int posQuestion = fileName.indexOf('?');
            final File theFile = getFile(posQuestion, fileName);

            sendResponse(theFile, version, method, contentType);

        } else {
            sendHeader(HttpResponseHeader.notAllowed());
        }
    }

    private void sendFile(File theFile) throws IOException {
        try( final InputStream ios= new FileInputStream(theFile)){
            final byte[] buffer = new byte[1024];

            int read= 0;
            while ((read = ios.read(buffer)) != -1) {
                raw.write(buffer, 0, read);
                raw.flush();
            }

        }catch (IOException e){
            sendHeader(HttpResponseHeader.notFound());
            System.out.println("problem with read file");
        }
    }

    private void sendResponse(File theFile, String version, String method, String contentType) throws IOException {
        System.out.println(theFile.exists());
        System.out.println(theFile.canRead());
        System.out.println(theFile.getCanonicalPath().startsWith(root.substring(1)));
        System.out.println(slashAfterFileName);
        if ((!slashAfterFileName) /*&& (theFile.canRead())*/ && (theFile.getCanonicalPath().startsWith(root.substring(1)))) {
            //final byte[] theData = Files.readAllBytes(Paths.get(theFile.toURI()));
            //final byte[] theData = readFile(theFile);
            sendHeader(HttpResponseHeader.ok((int) theFile.length(), contentType));
            if (method.toUpperCase().equals("GET")) {
                sendFile(theFile);
                //sendData(theData);
            }

        } else {
            if (tokens[1].equals("/httptest/")) {
                sendHeader(HttpResponseHeader.serverOK());
            } else {
                if (flagIndex) {
                    sendHeader(HttpResponseHeader.forbidden());
                } else {
                    sendHeader(HttpResponseHeader.notFound());
                }
            }

        }
    }

    private void parseRequest() throws IOException {
        this.tokens = readRequest().split("\\s+");
    }

    private String readRequest() throws IOException {
        final StringBuilder requestLine = new StringBuilder();
        while (true) {
            int c = in.read();
            if (c == '\r' || c == '\n') break;
            requestLine.append((char) c);
        }
        return requestLine.toString();
    }

    ServeClient(Socket socket) {
        this.socket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.raw = new BufferedOutputStream(socket.getOutputStream());
            this.out = new OutputStreamWriter(raw);
        } catch (IOException e) {
            System.out.println("problem with in/out");
        }
    }

    public void execute() {
        try {
            parseRequest();
            if (tokens.length >= 3) {
                Response();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            finish();
        }
    }

    private void finish() {
        try {
            in.close();
            out.close();
            raw.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Socket can not close;");
        }
    }

    public static void initTypeFiles() {
        final HashMap<String, String> typeFiles = new HashMap<>();

        typeFiles.put("html", "text/html");
        typeFiles.put("css", "text/css");
        typeFiles.put("js", "text/javascript");
        typeFiles.put("html", "text/html");
        typeFiles.put("jpg", "image/jpeg");
        typeFiles.put("jpeg", "image/jpeg");
        typeFiles.put("png", "image/png");
        typeFiles.put("gif", "image/gif");
        typeFiles.put("swf", "application/x-shockwave-flash");

        ServeClient.typeFiles = typeFiles;

    }


    private void sendHeader(String responseHeader) throws IOException {
        out.write(responseHeader);
        out.flush();
    }

    private void sendData(byte[] theData) throws IOException {
        raw.write(theData);
        raw.flush();
    }
}
