import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class Server {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private static final String BASE_DIRECTORY = "D:\\Codes\\HTTP\\src\\serverfiles"; 


    public static void main(String[] args) throws IOException {
        Server server = new Server(8080);
        System.out.println("Server running on port 8080");
        server.start();
    }

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(10); 
    }

    public void start() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            threadPool.submit(() -> handleRequest(clientSocket));
        }
    }

    private void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream())) {

            String requestLine = in.readLine();
            System.out.println("Request Line: " + requestLine);
            String method = requestLine.split(" ")[0];
            String fileName = requestLine.split(" ")[1].substring(1); // remove leading "/"

            if ("GET".equals(method)) {
                handleGetRequest(fileName, out, clientSocket);
            } else if ("POST".equals(method)) {
                handlePostRequest(in, out, fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    

    private void handleGetRequest(String fileName, PrintWriter out, Socket clientSocket) throws IOException {
        File file = new File(BASE_DIRECTORY, fileName);
        if (file.exists() && !file.isDirectory()) {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Length: " + fileContent.length);
            out.println("");
            out.flush();
            clientSocket.getOutputStream().write(fileContent, 0, fileContent.length);
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("");
        }
    }

    private void handlePostRequest(BufferedReader in, PrintWriter out, String fileName) throws IOException {
        StringBuilder headers = new StringBuilder();
        String line;
        int contentLength = 0;

        // Read headers and determine content length
        while (!(line = in.readLine()).isEmpty()) {
            headers.append(line).append("\n");
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
            System.out.println("Header: " + line);
        }

        char[] body = new char[contentLength];
        in.read(body, 0, contentLength);
        String bodyContent = new String(body);
        System.out.println("Body Content: " + bodyContent);

        // Write the body to a file in the specified folder
        File file = new File(BASE_DIRECTORY, fileName);
        Files.write(file.toPath(), bodyContent.getBytes());
        out.println("HTTP/1.1 201 Created");
        out.println("");
    }

}
