package project.server;

import java.io.*;
import java.net.*;

import static project.common.Constants.*;
import static project.common.Constants.DELETE_FILE_COMMAND;

class ServerReader implements Runnable {

    private Socket socket;
    private ServerClientsManager clientsManager;

    private InetAddress address;
    private DataInputStream input;

    public ServerReader(Socket socket, ServerClientsManager clientsManager) {
        this.socket = socket;
        this.clientsManager = clientsManager;
    }

    public void run() {
        try {
            address = socket.getInetAddress();
            input = new DataInputStream(socket.getInputStream());
            int command = input.readInt();

            if(command==LOGIN_COMMAND) {
                String login = input.readUTF();
                System.out.println(">> receiving login " + login);
                clientsManager.addClient(address, login);
                ServerClient client = clientsManager.getClient(address);
                client.sendLoginSuccess();
                client.sendAdvertisements();
            }

            else if(command==LOGOUT_COMMAND) {
                clientsManager.removeClient(address);
            }


            else if(command==SEND_FILE_COMMAND) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();
                System.out.println(">> receiving file " + relativePath);

                ServerClient client = clientsManager.getClient(address);
                ServerUser user = client.getUser();
                user.receiveFileData(relativePath, modificationTime, size, input);
                user.sendFileExcept(relativePath, client);
            }

            else if(command==SEND_FILE_TO_USER_COMMAND) {
                String login = input.readUTF();
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();
                System.out.println(">> receiving file to user " + login + " - " + relativePath);

                ServerUser user = clientsManager.getUser(login);
                if(user != null) {
                    user.receiveFileData(relativePath, modificationTime, size, input);
                    user.sendFileEveryone(relativePath);
                }
            }

            else if(command==NEED_FILE_COMMAND) {
                ServerClient client = clientsManager.getClient(address);
                String relativePath = input.readUTF();
                System.out.println(">> receiving need" + relativePath);
                client.sendFile(relativePath);
            }

            else if(command==DELETE_FILE_COMMAND) {
                String relativePath = input.readUTF();
                System.out.println(">> receiving delete " + relativePath);
                ServerClient client = clientsManager.getClient(address);
                ServerUser user = client.getUser();
                boolean deleted = user.deleteFile(relativePath);
                if(deleted) user.sendDeleteExcept(relativePath, client);
            }

            else if(command==CHECK_FILE_COMMAND) {
                String relativePath = input.readUTF();
                System.out.println(">> receiving check " + relativePath);
                long modificationTime = input.readLong();

                ServerClient client = clientsManager.getClient(address);
                if(!client.getUser().checkFile(relativePath, modificationTime))
                    client.sendRequest(relativePath);
            }

        } catch(IOException e) {
            System.err.println("Client has probably disconnected");
            e.printStackTrace();
        }
    }
}