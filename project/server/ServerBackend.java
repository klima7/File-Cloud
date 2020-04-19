package project.server;

import java.io.*;
import java.net.*;
import static project.common.Constants.*;

public class ServerBackend {

    private int port;
    private String rootDirectory;
    private ServerSocket serverSocket;
    private Thread acceptingThread;
    private ServerClientsManager clientsManager;

    public ServerBackend(String rootDirectory, int port) {
        this.port = port;
        this.rootDirectory = rootDirectory;
        this.clientsManager = new ServerClientsManager(this);

        File directory = new File(rootDirectory);
        if(!directory.exists())
            directory.mkdirs();
    }

    public int getPort() {
        return port;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port, 256, InetAddress.getByName(SERVER_ADDRESS));
        acceptingThread = new Thread(new ServerAccepter(serverSocket, clientsManager));
        acceptingThread.run();
    }

    public void shutdownServer() {
        acceptingThread.interrupt();
    }

}
