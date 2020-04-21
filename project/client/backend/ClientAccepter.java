package project.client.backend;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class ClientAccepter implements Runnable {

    private ServerSocket serverSocket;
    private ClientBackend clientBackend;

    private ExecutorService executor = Executors.newCachedThreadPool();

    public ClientAccepter(ServerSocket serverSocket, ClientBackend clientBackend) {
        this.serverSocket = serverSocket;
        this.clientBackend = clientBackend;
    }

    public void run() {
        try {
            while(!Thread.interrupted()) {
                Socket socket = serverSocket.accept();
                executor.execute(new ClientReader(socket, clientBackend));
            }
        } catch(IOException e) {
            System.err.println("Socket has been probably closed. Accepting Thread stopped!");
        }
    }
}