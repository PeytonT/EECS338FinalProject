import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;
import java.util.concurrent.*;
import java.math.BigInteger;

// Runs as a thread handling a connection with a client
public class ClientHandler extends Thread{

  private Socket client;
  private ConcurrentHashMap<String,User> users;
  private String username;
  private String password;
  private String user;
  private String modulus;
  private String public_key;

  // A clienthandler takes a socket that connects to the client and the hashmap of users of the server
  public ClientHandler(Socket socket, ConcurrentHashMap<String,User> users){

    // Connection to the client
    this.client = socket;
    // The table of users
    this.users = users;

  }
  
  public void run() {

    try {

      // Writer object so that we can stream strings to the client
      PrintWriter out = new PrintWriter(this.client.getOutputStream(), true);
      // Reader so that we can read lines from the client
      BufferedReader in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
      // Contains the protocol messages sent by the client
      String request;

      //Process the user attempting to log in
      processLogin(in, out);

      // Read the contents of the InputStream into request
      while ((request = in.readLine()) != null){

          // hand off to the appropriate protocol method
          processRequest(in, out, request);

      }
    } catch (IOException e) {
        System.err.println(e);
    }
    // If the while loop has exited it must mean the socket connection has been broken, so logout.
    this.users.get(this.user).logOut();
    return;
  }

  // Handles the protocol exchange to allow the client to provide information and log in.
  public void processLogin(BufferedReader in, PrintWriter out) throws IOException {

      // Get the client's user's username and print it
      System.out.println("Processing a login.");
      username = in.readLine();
      System.out.println("Username:");
      System.out.println(username);
      // Get the client's user's password
      password = in.readLine();
      // Get the client's user's RSA modulus and print it
      modulus = in.readLine();
      System.out.println("Modulus:");
      System.out.println(modulus);
      // Get the client's user's RSA public key and print it
      public_key = in.readLine();
      System.out.println("Public Key:");
      System.out.println(public_key);
      // If the username is already in the user table
      if (users.containsKey(username)){
        // If the provided password is correct
        if (users.get(username).getPassword().equals(password)){
          // Inform the client that the login is approved
          out.println("Accepted");
          // Set the user for this thread to the provided username
          this.user = username;
          // And log that user in
          this.users.get(this.user).logIn();
        // Otherwise, inform the client that their user has failed to log in.
        } else {
          out.println("Rejected");
        }
      // If the user is not already in the user table...
      } else {
        // Accept their connection
        out.println("Accepted");
        // Add them to the table, set them to the thread's user, and log them in
        users.put(username, new User(username, password, modulus, public_key));
        this.user = username;
        this.users.get(this.user).logIn();
      }
      System.out.println("Login processing complete.");
      return;
  }

  // Handles the client sending protocol requests
  public void processRequest(BufferedReader in, PrintWriter out, String request)
    throws IOException {
      // Handles the client asking for the table of user information
      if (isListRequest(request)){
        processListRequest(in, out);
      }
      // Handles the client sending a message to a user
      else if (isMessageRequest(request)){
        processMessageRequest(in, out);
      // Handles the client requesting to read their next message
      } else if (isReadRequest(request)){
        processReadRequest(in, out);
      // Handles the client making a malformed request (should not happen in the Client protocol)
      } else {
        processBadRequest(in, out);
      }
  }

  // Is the request for the user list?
  public boolean isListRequest(String request){
    return request.equals("list");
  }

  // Is the request a message to be sent?
  public boolean isMessageRequest(String request){
    return request.equals("message");
  }

  // Is the request for the next stored message?
  public boolean isReadRequest(String request){
    return request.equals("read");
  }

  // Handles the protocol for sending the list of users and their statuses
  public void processListRequest(BufferedReader in, PrintWriter out) throws IOException {
    // holds the output as it is assembled
    StringBuilder user_data = new StringBuilder();
    // Get all the users names
    Set<String> usernames = this.users.keySet();
    // Check if they are logged in or out and append appropriately
    for (String username: usernames){
      if (this.users.get(username).isOnline()){
        user_data.append(username + " - Online\n");
      } else {
        user_data.append(username + " - Offline\n");
      }
    }
    // Send the result
    out.println(user_data.toString());
    System.out.println("A list request was processed.");
    return;
  }

  // Handles the protocol for processing a message sent by the clientt
  public void processMessageRequest(BufferedReader in, PrintWriter out) throws IOException {
    // First message is the username of the recipient
    String recipient = in.readLine();
    // Get the recipients user file
    User target = this.users.get(recipient);
    if (target != null){
      // Get the modulus and public key from the client
      out.println(target.getModulus());
      out.println(target.getPublicKey());
      String message = in.readLine();
      // Send the sender and message to the recipient
      target.sendMessage(this.user + ":" + message);
      // Print the message sent to the recipient as an example for demonstrating the encryption
      System.out.println(this.user + ":" + message);
    } else {
      out.println("No such user.");
    }
    System.out.println("A message request was processed.");
    return;
  }

  // Handles the protocol for processing a request for the next stored message sent to the client's user
  public void processReadRequest(BufferedReader in, PrintWriter out) throws IOException {
    // Get the message
    String message = this.users.get(this.user).getNextMessage();
    // If there was a message
    if (message != null){
      // Send th emessage
      out.println(message);
      System.out.println("A read request was processed and a message was sent.");
    }
    else{
      // Otherwise send the empty string
      out.println("");
    }
    return;
  }

  public void processBadRequest(BufferedReader in, PrintWriter out) throws IOException {
    return;
  }



}