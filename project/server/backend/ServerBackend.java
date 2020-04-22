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

    public ServerBackend(String rootDirectory, int port) {
        this.port = port;
        this.rootDirectory = rootDirectory;
        this.clientsManager = new ServerClientsManager(rootDirectory, port);

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

    public void setServerListener(ServerListener serverListener) {
        this.serverListener = serverListener;
        clientsManager.setServerListener(serverListener);

        if(serverListener!=null) {
            serverListener.log("PO2 Project Server, Welcome");
        }
    }

    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port, 256, InetAddress.getByName(SERVER_ADDRESS));
        acceptingThread = new Thread(new ServerAccepter(serverSocket, clientsManager));
        acceptingThread.start();
    }

    public void shutdownServer() throws IOException {
        serverSocket.close();
        acceptingThread.interrupt();
    }

}
