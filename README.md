Write:
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

// Client Class
class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);
        ) {
            System.out.println("Connected to the chat server. Type your messages below and press Enter to send. Type 'exit' to leave the chat.");

            Thread listener = new Thread(() -> {
                try {
                    String message;
                    while ((message = serverReader.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.err.println("Disconnected from server.");
                }
            });

            listener.start();

            String userInput;
            while ((userInput = consoleReader.readLine()) != null) {
                serverWriter.println(userInput);
                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("You have left the chat.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}

Using the app:
Initializing the chat Server:
 







Connecting all guests:
 
 
 

All user connected to the chat:
 
User chatting:
 
   
 

 
Disconnecting from the server:
 
 
 
 

Explanation of the Online Chat Application
The chat application is a simple program that allows multiple users to connect to a server and exchange messages. The system is split into two main components: the ChatServer and the ChatClient. Let’s break down each part.
ChatServer: The Heart of the Application
The ChatServer is the core of the application, responsible for managing client connections and broadcasting messages.
1.	Server Setup:
o	The server runs on port 12345. It uses a ServerSocket to listen for incoming client connections.
o	When a new client connects, the server assigns a unique user ID to the client using a counter (userIdCounter). This helps distinguish between users.
2.	Handling Clients:
o	Each client is managed in its own thread using the ClientHandler class. This ensures that the server can handle multiple users simultaneously.
o	The ConcurrentHashMap is used to store active clients. Each entry consists of a user ID and a PrintWriter for sending messages to the client. This data structure is thread-safe, meaning it can handle concurrent access without issues.
3.	Message Broadcasting:
o	The server listens for messages from clients. When a message is received, it appends a timestamp (formatted as HH:mm:ss) and broadcasts the message to all connected clients.
o	If a client sends the message exit, the server removes them from the list of active clients, notifies others of their disconnection, and stops handling their input.
ChatClient: The User Interface
The ChatClient allows users to connect to the server, send messages, and receive updates in real-time.
1.	Connecting to the Server:
o	The client establishes a connection to the server running on localhost (the same computer) at port 12345. In real-world applications, the server's address might be a remote IP.
2.	User Interaction:
o	The user can type messages in the console, which are sent to the server. Typing exit lets the user leave the chat gracefully, both from the server and their own client application.
3.	Listening for Messages:
o	The client creates a separate thread to listen for messages from the server. This allows users to send and receive messages concurrently, ensuring a smooth chat experience.
4.	Clean Exit:
o	When a user disconnects, the client ensures all resources (like sockets and readers) are closed to prevent resource leaks.
Key Features
1.	Real-time Communication:
o	Messages are sent and received in real-time, making the chat experience interactive.
2.	Thread Safety:
o	The server uses ConcurrentHashMap to handle multiple clients simultaneously without data corruption.
3.	Timestamped Messages:
o	Every message is timestamped, making it easier to follow the conversation.
4.	Graceful Disconnection:
o	Both the server and client handle disconnections cleanly. Users can leave by typing exit, and the server notifies others about the departure.

Code Highlights
•	Server's broadcastMessage:
private void broadcastMessage(String message) {
    for (PrintWriter writer : clients.values()) {
        writer.println(message);
    }
}
This method ensures that all connected clients receive a copy of every message.
•	Client's Listener Thread:
Thread listener = new Thread(() -> {
    try {
        String message;
        while ((message = serverReader.readLine()) != null) {
            System.out.println(message);
        }
    } catch (IOException e) {
        System.err.println("Disconnected from server.");
    }
});
This thread keeps listening for new messages from the server and prints them to the console.

Practical Applications
This type of application is foundational in understanding socket programming and network communication. While this example is simple, similar techniques are used in real-world applications like messaging apps, multiplayer games, and collaborative tools.
Limitations and Possible Improvements
1.	Scalability:
o	The server might struggle with a very large number of clients due to resource constraints. Adding asynchronous processing or using non-blocking sockets could improve scalability.
2.	Security:
o	Currently, the chat is not encrypted, meaning messages are vulnerable to interception. Adding encryption (e.g., SSL/TLS) would enhance security.
3.	Advanced Features:
o	User authentication, private messaging, or a graphical user interface (GUI) could make the application more user-friendly and feature-rich.
References
•	Deitel, H. M., & Deitel, P. J. (2018). Java: How to Program (11th ed.). Pearson Education.
•	Schildt, H. (2018). Java: The Complete Reference (11th ed.). McGraw-Hill Education.

