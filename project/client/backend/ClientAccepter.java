package project.client.backend;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class ClientAccepter implements Runnable {

    private ServerSocket serverSocket;
    private ClientBackend clientBackend;
    private ClientListener clientListener;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public ClientAccepter(ServerSocket serverSocket, ClientBackend clientBackend, ClientListener clientListener) {
        this.serverSocket = serverSocket;
        this.clientBackend = clientBackend;
        this.clientListener = clientListener;
    }

    public void run() {
        try {
            while(!Thread.interrupted()) {
                Socket socket = serverSocket.accept();
                executor.execute(new ClientReader(socket, clientBackend, clientListener));
            }
        } catch(IOException e) {
            System.err.println("Socket has been probably closed. Accepting Thread stopped!");
        }
    }
}