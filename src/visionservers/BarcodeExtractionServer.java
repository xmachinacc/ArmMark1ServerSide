package visionservers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


import parsers.BarcodeParser;
import qrutils.QRCollection;
public class BarcodeExtractionServer {
    
    private final ServerSocket serverSocket;
    
    //private final Socket socket;
    
    //private final BufferedReader in;
    //private final OutputStream out;
    
    public BarcodeExtractionServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        //socket = serverSocket.accept();////
        //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //out = socket.getOutputStream(); 
    }
    
    public QRCollection extractBarcodes(byte[] image, int width, int height) throws IOException, InterruptedException {
        
        Socket socket = serverSocket.accept();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream out = socket.getOutputStream(); 
        
        out.write(image);
        String line = in.readLine();
        QRCollection barcodes;
        if(line.equals("[]")){
            barcodes = new QRCollection(new ArrayList<>());
        }else{
            barcodes = BarcodeParser.parse(line, width, height);
            
        }
        
        out.close();
        in.close();
        
        return barcodes;
        
        
        
        
        
    }
    
}
