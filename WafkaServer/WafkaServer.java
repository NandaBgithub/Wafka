package WafkaServer;

import java.net.*;
import java.io.*;
import java.util.*;

public class WafkaServer {
    
    public static void main(String[] args){
        int port = 3000;
        boolean isRunning = true;
        try (
            ServerSocket wafkaServer = new ServerSocket(port);
        ){
            Socket client = wafkaServer.accept();
            DataInputStream in = new DataInputStream(client.getInputStream());
            // OutputStream out = client.getOutputStream();

            while (isRunning){
                // Reading the first 4 bytes in input to determine the size of the buffer
                while (true){
                    int length;

                    try{
                        length = in.readInt();
                    } catch(EOFException e){
                        System.out.println("client disconnects");
                        break;
                    }

                    if (length <= 0 || length > 4000){
                        System.out.println("Length of input is invalid");
                        break;
                    }

                    byte[] event = new byte[length];
                    in.readFully(event);
                    System.out.println("Message recieved length = " + length);
                    System.out.println(Arrays.toString(event));
                }
            }
        } catch (IOException e) {
            System.out.println("Server failed to open");
            e.printStackTrace();
        }
    }
    
    
}
