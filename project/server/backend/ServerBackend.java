package project.server.backend;

import java.io.*;
import java.net.*;
import static project.common.Constants.*;

public class ServerBackend {

    private int port;
    private String rootDirectory;
    private ServerSocket serverSocket;
    private Thread acceptingThread;
    private ServerClientsManager clientsManager;
    private ServerListener serverListener;

    public ServerBackend(String rootDirectory, int port, ServerListener serverListener) {
        this.port = port;
        this.rootDirectory = rootDirectory;
        this.clientsManager = new ServerClientsManager(rootDirectory, port, serverListener);
        this.serverListener = serverListener;

        File directory = new File(rootDirectory);
        if(!directory.exists())
            directory.mkdirs();

        serverListener.log("# Server running on port " + port);
    }

    public int getPort() {
        return port;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port, 256, InetAddress.getByName(SERVER_ADDRESS));
        acceptingThread = new Thread(new ServerAccepter(serverSocket, clientsManager, serverListener));
        acceptingThread.start();
    }

    public void shutdownServer() throws IOException {
        clientsManager.sendServerDownEveryone();
        serverSocket.close();
        acceptingThread.interrupt();
    }
}
