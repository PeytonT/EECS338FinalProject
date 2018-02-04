import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;
import java.util.concurrent.*;

// Runs as a seperate thread, taking user input and adding it to a buffer for the main Client process to handle
public class ClientInputHandler extends Thread{

  // The concurrent buffer
  private ConcurrentLinkedQueue<String> input_buffer;

  public ClientInputHandler(ConcurrentLinkedQueue<String> input_buffer){

    // Since the buffer is taken in the constructor, it is shared with the parent
    this.input_buffer = input_buffer;

  }
  
  // Runs until exit
  public void run() {

      Scanner sc = new Scanner(System.in);
      String next_line;


      while (true){
        // Scans for user input
        next_line = sc.nextLine();
        // Adds it to the buffer
        input_buffer.add(next_line);
        // breaks if the user commanded an exit
        if (next_line.equals("exit")){
          break;
        }

      }
    // ends the thread if reached
    return;

  }

}