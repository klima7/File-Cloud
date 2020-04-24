package project.client.backend;

import java.io.*;
import java.net.*;
import static project.common.Constants.*;

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


            if (command == LOGIN_SUCCESS_COMMAND) {
                clientListener.log(">> receiving login success");
                clientBackend.sendFileCheckAll();
            }

            else if (command == CHECK_FILE_COMMAND) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                clientListener.log(">> receiving advertisement for file " + relativePath);
                if(!clientBackend.isFileUpToDate(relativePath, modificationTime))
                    clientBackend.sendFileRequest(relativePath);
            }

            else if (command == NEED_FILE_COMMAND) {
                String relativePath = input.readUTF();
                clientListener.log(">> receiving request for file " + relativePath);
                clientBackend.sendFileData(relativePath);
            }

            else if (command == SEND_FILE_COMMAND) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();
                clientListener.log(">> receiving file " + relativePath);
                clientBackend.receiveFile(relativePath, modificationTime, size, input);
            }

            else if (command == DELETE_FILE_COMMAND) {
                String relativePath = input.readUTF();
                clientListener.log(">> receiving delete request for file " + relativePath);
                clientBackend.deleteFile(relativePath);
            }

            else if (command == USER_ACTIVE_COMMAND) {
                String login = input.readUTF();
                clientListener.log(">> receiving active user " + login);
                clientBackend.getUsersTracer().addActiveUser(login);
            }

            else if (command == USER_INACTIVE_COMMAND) {
                String login = input.readUTF();
                clientListener.log(">> receiving inactive user " + login);
                clientBackend.getUsersTracer().removeActiveUser(login);
            }

            else if (command == SERVER_DOWN_COMMAND) {
                clientListener.errorOccured();
            }

        } catch (IOException e) {
            System.err.println("Client has probably disconnected");
        }
    }
}