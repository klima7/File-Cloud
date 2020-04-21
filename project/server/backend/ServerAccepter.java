package project.server.backend;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class ServerAccepter implements Runnable {

    private ServerSocket serverSocket;
    private ServerClientsManager clientsManager;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public ServerAccepter(ServerSocket serverSocket, ServerClientsManager clientsManager) {
        this.serverSocket = serverSocket;
        this.clientsManager = clientsManager;
    }

    public void run() {
        try {
            while(!Thread.interrupted()) {
                Socket socket = serverSocket.accept();
                executor.execute(new ServerReader(socket, clientsManager));
            }
        } catch(IOException e) {
            executor.shutdownNow();
            System.out.println("<> Server accepter stopped");
        }
    }
}