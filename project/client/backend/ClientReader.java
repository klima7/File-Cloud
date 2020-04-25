package project.client.backend;

import project.common.Command;

import java.io.*;
import java.net.*;

class ClientReader implements Runnable {

    private Socket socket;
    private ClientBackend clientBackend;
    private ClientListener clientListener;

    public ClientReader(Socket socket, ClientBackend clientBackend, ClientListener clientListener) {
        this.socket = socket;
        this.clientBackend = clientBackend;
        this.clientListener = clientListener;
    }

    public void run() {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            int command = input.readInt();


            if (command == Command.LOGIN_SUCCESS.asInt()) {
                clientListener.log(">> receiving login success");
                clientBackend.sendFileCheckAll();
            }

            else if (command == Command.CHECK_FILE.asInt()) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                clientListener.log(">> receiving advertisement for file " + relativePath);
                if(!clientBackend.isFileUpToDate(relativePath, modificationTime))
                    clientBackend.sendFileRequest(relativePath);
            }

            else if (command == Command.NEED_FILE.asInt()) {
                String relativePath = input.readUTF();
                clientListener.log(">> receiving request for file " + relativePath);
                clientBackend.sendFileData(relativePath);
            }

            else if (command == Command.SEND_FILE.asInt()) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();
                clientListener.log(">> receiving file " + relativePath);
                clientBackend.receiveFile(relativePath, modificationTime, size, input);
            }

            else if (command == Command.DELETE_FILE.asInt()) {
                String relativePath = input.readUTF();
                clientListener.log(">> receiving delete request for file " + relativePath);
                clientBackend.deleteFile(relativePath);
            }

            else if (command == Command.USER_ACTIVE.asInt()) {
                String login = input.readUTF();
                clientListener.log(">> receiving active user " + login);
                clientBackend.getUsersTracer().addActiveUser(login);
            }

            else if (command == Command.USER_INACTIVE.asInt()) {
                String login = input.readUTF();
                clientListener.log(">> receiving inactive user " + login);
                clientBackend.getUsersTracer().removeActiveUser(login);
            }

            else if (command == Command.SERVER_DOWN.asInt()) {
                clientListener.errorOccured();
            }

        } catch (IOException e) {
            System.err.println("Client has probably disconnected");
        }
    }
}