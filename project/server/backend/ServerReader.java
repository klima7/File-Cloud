package project.server.backend;

import java.io.*;
import java.net.*;

import static project.common.Constants.*;
import static project.common.Constants.DELETE_FILE_COMMAND;

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

            if(command==LOGIN_COMMAND) {
                String login = input.readUTF();
                serverListener.log(">> receiving login request from " + address.getHostName() + "(" + login + ")" + " to " + login);
                clientsManager.addClient(address, login);
                ServerClient client = clientsManager.getClient(address);
                client.sendLoginSuccess();
                client.sendAdvertisements();
            }

            else if(command==LOGOUT_COMMAND) {
                serverListener.log(">> receiving logout from " + clientsManager.getClient(address));
                clientsManager.removeClient(address);
            }


            else if(command==SEND_FILE_COMMAND) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();

                ServerClient client = clientsManager.getClient(address);
                ServerUser user = client.getUser();
                serverListener.log(">> receiving file " + relativePath + " from " + client);
                user.receiveFileData(relativePath, modificationTime, size, input);
                user.sendFileExcept(relativePath, client);
            }

            else if(command==SEND_FILE_TO_USER_COMMAND) {
                String login = input.readUTF();
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();

                ServerUser user = clientsManager.getUser(login);
                serverListener.log(">> receiving file " + relativePath + " from " + address.getHostName() + "(" + login + ")" + " to " + login);
                if(user != null) {
                    user.receiveFileData(relativePath, modificationTime, size, input);
                    user.sendFileEveryone(relativePath);
                }
            }

            else if(command==NEED_FILE_COMMAND) {
                ServerClient client = clientsManager.getClient(address);
                String relativePath = input.readUTF();
                serverListener.log(">> receiving file request for " + relativePath + " from " + client);
                client.sendFile(relativePath);
            }

            else if(command==DELETE_FILE_COMMAND) {
                String relativePath = input.readUTF();
                ServerClient client = clientsManager.getClient(address);
                serverListener.log(">> receiving delete request for " + relativePath + " from " + client);
                ServerUser user = client.getUser();
                boolean deleted = user.deleteFile(relativePath);
                if(deleted) user.sendDeleteExcept(relativePath, client);
            }

            else if(command==CHECK_FILE_COMMAND) {
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