package visionservers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import ballutils.BallCollection;
import parsers.BallParser;

public class BallExtractionServer {

    private final ServerSocket serverSocket;

    public BallExtractionServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public BallCollection extractBalls(byte[] image, int width, int height) throws IOException, InterruptedException {

        Socket socket = serverSocket.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream out = socket.getOutputStream();

        out.write(image);
        String line = in.readLine();
        BallCollection balls = new BallCollection(new ArrayList<>());
        if (line != null){
        if (line.equals("[]")) {
            balls = new BallCollection(new ArrayList<>());
        } else {
            balls = BallParser.parse(line, width, height);

        }
        }

        out.close();
        in.close();

        return balls;

    }

}

