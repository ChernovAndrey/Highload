import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by andrey on 15.10.17.
 */
public class ServeClient{
    public static  String root = "/home/andrey/IdeaProjects/HighloadMaven";
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
        if (posQuestion == -1) {
            return new File( fileName);
                   // fileName.substring(1, fileName.length()));
        } else {
            return new File(fileName.substring(0,root.length()+posQuestion));
                   // fileName.substring(1, root.length()+posQuestion));
        }
    }

    private void Response() throws IOException {
        final String method = tokens[0];
        if (method.toUpperCase().equals("GET") || (method.toUpperCase().equals("HEAD"))) {
            String fileName = URLDecoder.decode(tokens[1], "UTF-8");
            final int posLastDot = fileName.lastIndexOf('.');

            String contentType;
            if(posLastDot!=-1){
                try {
                    contentType = typeFiles.get(fileName.substring(posLastDot + 1));
                }catch (Exception e){
                    System.out.println("unnsuport type="+fileName.substring(posLastDot + 1));
                    sendHeader(HttpResponseHeader.notAllowed());
                    return;
                }
            }else{
                if(fileName.endsWith("/")){
                    flagIndex=true;
                    fileName+=indexFileName;
                    contentType="html";
                }else{
                    sendHeader(HttpResponseHeader.forbidden());
                    return;
                }
            }

            final String version = tokens[2];
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
        }
    }

    private void sendResponse(File theFile, String version, String method, String contentType) throws IOException {
        if ((!slashAfterFileName) && (theFile.canRead()) && (theFile.getCanonicalPath().startsWith(root))) {
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
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.raw = new BufferedOutputStream(this.socket.getOutputStream());
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
        } catch (Exception e) {
            System.out.println("catch in execute");
            e.printStackTrace();
        } finally {
            finish();
        }
    }

    private void finish() {

        try{
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //    try {
          //  in.close();
          //  out.close();
          //  raw.close();
          //  socket.close();
    /*    } catch (IOException e) {
            System.out.println("Socket can not close;");
        }*/
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


    private void sendHeader(String responseHeader) {
       try {
           out.write(responseHeader);
           out.flush();
       }catch (Exception e){
           System.out.println("catch in sendHeader"+ e.getMessage());
       }
    }

    private void sendData(byte[] theData){
        try {
            raw.write(theData);
            raw.flush();
        }catch (Exception e){
            System.out.println("catch in sendData ");
        }
    }
}
