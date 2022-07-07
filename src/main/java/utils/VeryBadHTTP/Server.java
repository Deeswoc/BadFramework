package utils.VeryBadHTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            System.out.println("Server listening for new client...");
            new Server.ConnectionHandler(serverSocket.accept()).run();
            System.out.println("Client Connected!!");
        }

    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public static void main(String[] args) {
        final int PORT = 6969;
        Server server = new Server();
        server.listen(PORT, "Listening on port " + PORT);
    }

    private static class ConnectionHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ConnectionHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Request request = new Request(clientSocket.getInputStream());
                System.out.println("This line reached");

            } catch (IOException ex) {
                System.out.println("Issue making connection");
            }
        }
    }

    public void listen(int port, String startMessage) {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println(startMessage);
                new ConnectionHandler(serverSocket.accept()).run();
                System.out.println("Client Connected!!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen(int port) {
        listen(port, "");
    }
}
