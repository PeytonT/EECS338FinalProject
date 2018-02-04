import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

  public static void main(String[] args) throws IOException {

    // The first and only argument is the port number
    int port = Integer.parseInt(args[0]);
    // Create a concurrent hash map to store the information about the logged in users, which will be sent to each thread
    ConcurrentHashMap<String,User> users = new ConcurrentHashMap<String,User>();

    try {


      // Print a start-up message
      System.out.println("Starting the Server on port " + port);

      // And start running the server
      runServer(port, users); // never returns

    } catch (Exception e) {

      System.err.println(e);

    }
  }


  // Runs the server, accepting connections on the given port and creating threads to handle them
  public static void runServer(int port, ConcurrentHashMap<String,User> users)
      throws IOException {

    // Create a ServerSocket to listen for connections 
    ServerSocket ss = new ServerSocket(port);

    // Socket to hold created sockets handling incoming connections
    Socket client = null;

    while (true) {

      try {
        
        // Wait for a connection on the local port
        client = ss.accept();

        // Hand off the socket to a new thread
        new ClientHandler(client, users).start();

      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }
}
