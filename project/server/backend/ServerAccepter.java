package project.server.backend;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class ServerAccepter implements Runnable {

    private ServerSocket serverSocket;
    private ServerClientsManager clientsManager;
    private ServerListener serverListener;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public ServerAccepter(ServerSocket serverSocket, ServerClientsManager clientsManager, ServerListener serverListener) {
        this.serverSocket = serverSocket;
        this.clientsManager = clientsManager;
        this.serverListener = serverListener;
    }

    public void run() {
        try {
            while(!Thread.interrupted()) {
                Socket socket = serverSocket.accept();
                executor.execute(new ServerReader(socket, clientsManager, serverListener));
            }
        } catch(IOException e) {
            executor.shutdownNow();
            System.err.println("<> Server accepter stopped");
        }
    }
}