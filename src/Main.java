import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Server Class
class ChatServer {
    private static final int PORT = 12345;
    private static Map<Integer, PrintWriter> clients = new ConcurrentHashMap<>();
    private static int userIdCounter = 1;

    public static void main(String[] args) {
        System.out.println("Chat server started...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                int userId = userIdCounter++;
                System.out.println("User " + userId + " connected.");

                new ClientHandler(clientSocket, userId).start();
            }
        } catch (IOException e) {
            System.err.println("Error in server: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private int userId;

        public ClientHandler(Socket socket, int userId) {
            this.socket = socket;
            this.userId = userId;
        }

        @Override
        public void run() {
            try (
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                PrintWriter writer = new PrintWriter(output, true);
            ) {
                clients.put(userId, writer);

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        System.out.println("User " + userId + " has left the chat.");
                        writer.println("You have left the chat.");
                        break;
                    }

                    String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
                    System.out.println("[" + timestamp + "] User " + userId + ": " + message);
                    broadcastMessage("[" + timestamp + "] User " + userId + ": " + message);
                }

            } catch (IOException e) {
                System.err.println("User " + userId + " disconnected.");
            } finally {
                clients.remove(userId);
                broadcastMessage("User " + userId + " has disconnected.");
            }
        }

        private void broadcastMessage(String message) {
            for (PrintWriter writer : clients.values()) {
                writer.println(message);
            }
        }
    }
}