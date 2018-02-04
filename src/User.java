  import java.io.*;
  import java.net.*;
  import java.util.*;
  import java.util.concurrent.*;

  // Used to store a hostname bound to an IP address for DNS caching
  class User {
    // The user's username, used as the key to find them in the table
    String username;
    // The user's password (temporary until RSA implemented)
    String password;
    // The user's online status
    boolean online;
    // The user's message buffer, to which messages addressed to the user are written
    // The user's client will read from this buffer
    ConcurrentLinkedQueue<String> message_buffer;
    // The user's RSA modulus
    String modulus;
    // The user's RSA public key
    String public_key;

    public User(String username, String password, String modulus, String public_key){

      this.username = username;
      this.password = password;
      this.public_key = public_key;
      this.modulus = modulus;
      this.online = true;
      this.message_buffer = new ConcurrentLinkedQueue<String>();

    }

    public String getUsername(){
      return this.username;
    }

    public String getPassword(){
      return this.password;
    }

    public String getPublicKey(){
      return this.public_key;
    }

    public String getModulus(){
      return this.modulus;
    }

    public boolean isOnline(){
      return this.online;
    }

    public void logIn(){
      this.online = true;
      return;
    }

    public void logOut(){
      this.online = false;
      return;
    }

    // Used by other users to send messages to the user
    public void sendMessage(String message){
      this.message_buffer.add(message);
      return;
    }

    // Used by the server to serve requests from the client for the oldest unsent messaged addressed to it
    public String getNextMessage(){
      return this.message_buffer.poll();
    }

  }