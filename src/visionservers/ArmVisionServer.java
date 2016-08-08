package visionservers;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.common.io.ByteStreams;

public class ArmVisionServer {

    /** Socket for receiving incoming connections. */
    private final ServerSocket serverSocket;
    
    //private final Socket socket; ////
    
   
    public ArmVisionServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        //socket = serverSocket.accept(); ////
    }

    public byte[] getImageAsByteArray() throws IOException, InterruptedException {
        
            final Socket socket = serverSocket.accept();
            InputStream in = socket.getInputStream();
            byte[] bytes = ByteStreams.toByteArray(in);
            in.close();
            
            return bytes;
            
    }
    
}
