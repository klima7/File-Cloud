package project.server.backend;

import project.common.*;
import java.io.*;
import java.net.*;

class ServerReader implements Runnable {

    private Socket socket;
    private ServerClientsManager clientsManager;
    private ServerListener serverListener;

    private InetAddress address;
    private DataInputStream input;

    public ServerReader(Socket socket, ServerClientsManager clientsManager, ServerListener serverListener) {
        this.socket = socket;
        this.clientsManager = clientsManager;
        this.serverListener = serverListener;
    }

    public void run() {
        try {
            address = socket.getInetAddress();
            input = new DataInputStream(socket.getInputStream());
            int command = input.readInt();

            if(command==Command.LOGIN.asInt()) {
                String login = input.readUTF();
                serverListener.log(">> receiving login request from " + address.getHostName() + "(" + login + ")" + " to " + login);
                clientsManager.addClient(address, login);
                ServerClient client = clientsManager.getClient(address);
                client.sendLoginSuccess();
                client.sendAdvertisements();
            }

            else if(command==Command.LOGOUT.asInt()) {
                serverListener.log(">> receiving logout from " + clientsManager.getClient(address));
                clientsManager.removeClient(address);
            }


            else if(command==Command.SEND_FILE.asInt()) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();

                ServerClient client = clientsManager.getClient(address);
                ServerUser user = client.getUser();
                serverListener.log(">> receiving file " + relativePath + " from " + client);
                user.receiveFileData(relativePath, modificationTime, size, input);
                user.sendFileExcept(relativePath, client);
            }

            else if(command==Command.SEND_TO_USER.asInt()) {
                String login = input.readUTF();
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();

                ServerClient sendingClient = clientsManager.getClient(address);
                ServerUser user = clientsManager.getUser(login);
                serverListener.log(">> receiving file " + relativePath + " from " + sendingClient + " to " + login);
                if(user != null) {
                    relativePath += " (from " + sendingClient.getUser().getLogin() + ")";
                    user.receiveFileData(relativePath, modificationTime, size, input);
                    user.sendFileEveryone(relativePath);
                }
            }

            else if(command==Command.NEED_FILE.asInt()) {
                ServerClient client = clientsManager.getClient(address);
                String relativePath = input.readUTF();
                serverListener.log(">> receiving file request for " + relativePath + " from " + client);
                client.sendFile(relativePath);
            }

            else if(command==Command.DELETE_FILE.asInt()) {
                String relativePath = input.readUTF();
                ServerClient client = clientsManager.getClient(address);
                serverListener.log(">> receiving delete request for " + relativePath + " from " + client);
                ServerUser user = client.getUser();
                boolean deleted = user.deleteFile(relativePath);
                if(deleted) user.sendDeleteExcept(relativePath, client);
            }

            else if(command==Command.CHECK_FILE.asInt()) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();

                ServerClient client = clientsManager.getClient(address);
                serverListener.log(">> Checking if file " + relativePath + " from " + client + " is up to date");
                if(!client.getUser().checkFile(relativePath, modificationTime))
                    client.sendRequest(relativePath);
            }

        } catch(IOException e) {
            System.err.println("Client has probably disconnected");
            e.printStackTrace();
        }
    }
}