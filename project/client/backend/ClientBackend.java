package project.client.backend;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import static project.common.Constants.*;

public class ClientBackend {
    public static final String IP_GROUP = "Virtual Network1";
    public static final String IP_START = "127.0.0.2";

    private InetAddress addresIP;
    private int port;

    private String login;
    private String directory;

    private ServerSocket serverSocket;
    private Thread acceptingThread;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ClientWatcher clientWatcher;
    private ClientUsersTracer usersTracer = new ClientUsersTracer();
    private ClientListener clientListener;

    public ClientBackend(String login, String directory, int port) throws IOException {
        this.login = login;
        this.directory = directory;
        this.port = port;
        this.addresIP = VirtualIP.allocateIP(IP_GROUP, IP_START);
        clientWatcher = new ClientWatcher(this);
    }

    public String getDirectory() {
        return directory;
    }

    public String getLogin() {
        return login;
    }

    public ClientUsersTracer getUsersTracer() {
        return usersTracer;
    }

    public void setClientListener(ClientListener clientListener) {
        this.clientListener = clientListener;
        usersTracer.setClientListener(clientListener);
        clientWatcher.setClientListener(clientListener);
        clientListener.log("PO2 Project Client, Welcome");
    }

    public void start() throws IOException {
        System.out.println(addresIP);
        sendLogin(login);
        serverSocket = new ServerSocket(port, 256, addresIP);
        acceptingThread = new Thread(new ClientAccepter(serverSocket, this));
        acceptingThread.start();
    }

    public void stop() {
        acceptingThread.interrupt();
        sendLogout(login);
    }

    public void sendLogin(String login) {
        System.out.println("<< sending login " + login);
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(LOGIN_COMMAND);
                stream.writeUTF(login);
            }
        });
    }

    public void sendLogout(String login) {
        System.out.println("<< sending logout " + login);
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(LOGOUT_COMMAND);
                stream.writeUTF(login);
            }
        });
    }

    public void sendFileCheck(String relativePath) {
        System.out.println("<< sending check " + relativePath);
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();

        if(file.isDirectory())
            return;

        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(CHECK_FILE_COMMAND);
                stream.writeUTF(relativePath);
                stream.writeLong(modificationTime);
            }
        });
    }

    public void sendFileCheckAll() {
        String[] list = new File(directory).list();
        for(String name : list)
            sendFileCheck(name);
    }

    public void sendFileRequest(String relativePath) {
        System.out.println("<< sending need " + relativePath);
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(NEED_FILE_COMMAND);
                stream.writeUTF(relativePath);
            }
        });
    }

    public void sendFileDelete(String relativePath) {
        System.out.println("<< sending delete " + relativePath);
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(DELETE_FILE_COMMAND);
                stream.writeUTF(relativePath);
            }
        });
    }

    public void sendFileData(String relativePath, String login) {
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();
        long size = file.length();

        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                if(login == null) {
                    System.out.println("<< sending file " + relativePath);
                    stream.writeInt(SEND_FILE_COMMAND);
                }
                else {
                    System.out.println("<< sending file to " + login + " " + relativePath);
                    stream.writeInt(SEND_FILE_TO_USER_COMMAND);
                    stream.writeUTF(login);
                }

                stream.writeUTF(relativePath);
                stream.writeLong(modificationTime);
                stream.writeLong(size);

                try(FileInputStream input = new FileInputStream(file)) {
                    for(long pos=0; pos<size; pos++) {
                        int aByte = input.read();
                        stream.write(aByte);
                        stream.flush();
                    }
                }
            }
        });
    }

    public void sendFileData(String relativePath) {
        sendFileData(relativePath, null);
    }

    public void receiveFile(String relativePath, long modificationTime, long size, DataInputStream input) throws IOException{
        clientWatcher.addIgnore(relativePath);

        File newFile = new File(directory, relativePath);
        File parent = newFile.getParentFile();

        if(!parent.exists())
            parent.mkdirs();

        if(newFile.exists())
            newFile.delete();

        newFile.createNewFile();

        try(FileOutputStream output = new FileOutputStream(newFile)) {
            for (long pos = 0; pos < size; pos++) {
                int aByte = input.read();
                output.write(aByte);
            }
        } catch(IOException e) {
            System.err.println("IOException when receiving file");
            e.printStackTrace();
        }

        newFile.setLastModified(modificationTime);
        clientWatcher.removeIgnore(relativePath);

        if(clientListener!=null)
            clientListener.filesUpdated();
    }

    public boolean isFileUpToDate(String relativePath, long otherModificationTime) {
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();
        if(modificationTime < otherModificationTime)
            return false;
        else
            return true;
    }

    public void deleteFile(String relativePath) {
        clientWatcher.addIgnore(relativePath);
        File file = new File(directory, relativePath);
        file.delete();
        clientWatcher.removeIgnore(relativePath);

        if(clientListener!=null)
            clientListener.filesUpdated();
    }

    private abstract class SendWrapper implements Runnable {
        public void run() {
            executor.execute(() ->
            {
                try (Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), port, addresIP, 0)) {
                    DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                    send(stream);
                } catch (IOException e) {
                    System.err.println("Error occured while sending");
                }
            });
        }

        abstract void send(DataOutputStream stream) throws IOException;
    }
}
