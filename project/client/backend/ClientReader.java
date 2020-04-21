package project.client.backend;

import java.io.*;
import java.net.*;
import static project.common.Constants.*;

class ClientReader implements Runnable {

    private Socket socket;
    private ClientBackend clientBackend;

    public ClientReader(Socket socket, ClientBackend clientBackend) {
        this.socket = socket;
        this.clientBackend = clientBackend;
    }

    public void run() {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            int command = input.readInt();


            if (command == LOGIN_SUCCESS_COMMAND) {
                System.out.println(">> receiving login success");
                clientBackend.sendFileCheckAll();
            }

            else if (command == CHECK_FILE_COMMAND) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                System.out.println(">> receiving check " + relativePath);
                if(!clientBackend.isFileUpToDate(relativePath, modificationTime))
                    clientBackend.sendFileRequest(relativePath);
            }

            else if (command == NEED_FILE_COMMAND) {
                String relativePath = input.readUTF();
                System.out.println(">> receiving need " + relativePath);
                clientBackend.sendFileData(relativePath);
            }

            else if (command == SEND_FILE_COMMAND) {
                String relativePath = input.readUTF();
                long modificationTime = input.readLong();
                long size = input.readLong();
                System.out.println(">> receiving file " + relativePath);
                clientBackend.receiveFile(relativePath, modificationTime, size, input);
            }

            else if (command == DELETE_FILE_COMMAND) {
                String relativePath = input.readUTF();
                System.out.println(">> receiving delete " + relativePath);
                clientBackend.deleteFile(relativePath);
            }

            else if (command == USER_ACTIVE_COMMAND) {
                String login = input.readUTF();
                System.out.println(">> receiving active user " + login);
                clientBackend.getUsersTracer().addActiveUser(login);
            }

            else if (command == USER_INACTIVE_COMMAND) {
                String login = input.readUTF();
                System.out.println(">> receiving inactive user " + login);
                clientBackend.getUsersTracer().removeActiveUser(login);
            }

        } catch (IOException e) {
            System.err.println("Client has probably disconnected");
        }
    }
}