import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigInteger;

public class Client {

  public static void main(String[] args) throws IOException {

    String hostname;
    int port;
    String username;
    String password;
    ConcurrentLinkedQueue<String> input_buffer;

    // Parse the arguments to get the values for the user
    hostname = args[0];
    port = Integer.parseInt(args[1]);
    username = args[2];
    password = args[3];
    input_buffer = new ConcurrentLinkedQueue<String>();
    // For storing the RSA values
    BigInteger modulus;
    BigInteger public_key;
    BigInteger private_key;
    // For decryption
    RSA rsa;

    try {

      // Print a start-up message
      System.out.println("Starting the Client");

      // Read the contents of the user's RSA file into the relevant fields
      BufferedReader br = new BufferedReader(new FileReader(username + "_RSA"));
      modulus = new BigInteger(br.readLine());
      public_key = new BigInteger(br.readLine());
      private_key = new BigInteger(br.readLine());
      br.close();
      // Create the RSA decryption object
      rsa = new RSA(modulus, public_key, private_key);


      // And start running the client
      runClient(hostname, port, username, password, input_buffer, rsa); // never returns

    } catch (Exception e) {

      System.err.println(e);

    }
  }


  // Runs the client
  public static void runClient(String hostname, int port, String username, String password,
    ConcurrentLinkedQueue<String> input_buffer, RSA rsa) throws IOException {

    // Create a socket connecting to the server
    Socket server = new Socket(hostname, port);

    // Create the writer and reader to communicate with the server using strings
    PrintWriter out = new PrintWriter(server.getOutputStream(), true);
    BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));

    String next_input;

    // Log in by sending the relevant information to populate a User object for the user
    out.println(username);
    out.println(password);
    out.println(rsa.get_mod().toString());
    out.println(rsa.get_pub().toString());
    // Check if the attempt was successful
    String response = in.readLine();
    if (response.equals("Accepted")){
      System.out.println("Login successful.");
    } else {
      System.out.println("Login unsuccessful. You must have used a wrong password.");
      return;
    }

    // Create and run a new ClientInputHandler thread, which takes user entries and puts them in the input buffer for processing
    // This means that the user will always be able to type regardless of what the primary client process is doing
    new ClientInputHandler(input_buffer).start();

    while (true) {

      try {

        // If there is a user command waiting to be processed
        if (!input_buffer.isEmpty()){
          // Get the oldest command in the buffer
          next_input = input_buffer.poll();
          // If the command is 'exit', exit the client
          if (isExit(next_input)){
            System.out.println("Exiting...");
            out.close();
            in.close();
            return;
          }
          // Otherwise send it to the relevant protocol handler
          else {
            process_input(next_input, in, out);
          }
        }

        // Sleep for 1/10 of a second, Sleeping prevents the client from bombarding the server
        try {
            Thread.sleep(100);                 
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        // Try reading the next message to the user, print it if it exists
        displayMessages(in, out, rsa);

      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }

  // Determines which kind of command the user has input and directs the appropriate method to handle it
  public static void process_input(String input, BufferedReader in, PrintWriter out) throws IOException {
    // Display the help message
    if (isHelp(input)){
       printHelp();
    }
    // Request the user list
    else if (isList(input)){
       requestUsers(in, out);
    }
    // Send a message
    else if (isMessage(input)){
      sendMessage(in, out, input);
    }
    // Inform the user that their command is not recognized
    else {
      System.out.println("Improperly formatted input. Try entering 'help' to see options.");
    }
    return;
  }

  public static boolean isExit(String input){
    return input.equals("exit");
  }

  public static boolean isHelp(String input){
    return input.equals("help");
  }

  public static boolean isList(String input){
    return input.equals("list");
  }

  // Messages have the format "message:recipient_name:message_content"
  public static boolean isMessage(String input){
    String[] parts = input.split(":");
    return parts[0].equals("message");
  }

  // Print the help details if the user requests them
  public static void printHelp(){
    StringBuilder help = new StringBuilder();
    help.append("This encrypted chat client supports end-to-end 1024 bit RSA.\n");
    help.append("To create an RSA file for yourself, run the command 'java RSA username' with your preferred original username.\n");
    help.append("If you attempt to create an RSA file for a username that already exists you will not succeed.\n");
    help.append("To run the client (which someone must have done to get here), run the command,\n");
    help.append("'java Client address_of_server destination_port username password' from a folder where an RSA file exists for the username.\n");
    help.append("To get a list of users and their online/offline status, input 'list'.\n");
    help.append("To send a message, input 'message:username_of_recipient:your_message'.\n");
    help.append("To exit the program, input 'exit'.\n");
    help.append("To print this message again, input 'help'.");
    System.out.println(help.toString());
    return;
  }

  // Handles the user requesting the list of users
  public static void requestUsers(BufferedReader in, PrintWriter out) throws IOException{
    out.println("list");
    String line;
    while (!(line = in.readLine()).equals("")){
      System.out.println(line);
    }
    return;
  }
  // Messages have the format "message:recipient_name:message_content"
  public static void sendMessage(BufferedReader in, PrintWriter out, String input) throws IOException{
    // split the message into its components
    String[] parts = input.split(":");
    // If the message has 3 components
    if (parts.length == 3){
      // Send the message request and the destination user
      out.println(parts[0]);
      out.println(parts[1]);
      // Either receive the RSA modulus for the user or learn that the user does not exist
      String modulus = in.readLine();
      // If they exist
      if (!modulus.equals("No such user.")){
        // Get their public key
        String public_key = in.readLine();
        // Then encrypt the message and send it
        RSA encryptor = new RSA(new BigInteger(modulus), new BigInteger(public_key));
        out.println(encryptor.encrypt(parts[2]));
      } else {
        System.out.println("No such user.");
      }
    } else {
      System.out.println("Improperly formatted message. Try entering 'help'.");
    }
    return;
  }

  // Handles requesting the next pending message from the server
  public static String requestNextMessage(BufferedReader in, PrintWriter out) throws IOException{
    out.println("read");
    String message = in.readLine();
    return message;
  }

  // Handles printing the next pending message if there is one.
  public static void displayMessages(BufferedReader in, PrintWriter out, RSA rsa) throws IOException{
    String nextMessage;
    if (!(nextMessage = requestNextMessage(in, out)).equals("")){
      System.out.print(nextMessage.split(":")[0] + ": ");
      System.out.println(rsa.decrypt(nextMessage.split(":")[1]));
    }
    return;
  }


}
