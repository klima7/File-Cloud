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
    private ClientUsersTracer usersTracer;
    private ClientListener clientListener;

    public ClientBackend(String login, String directory, int port, ClientListener clientListener) throws IOException {
        this.login = login;
        this.directory = directory;
        this.port = port;
        this.addresIP = VirtualIP.allocateIP(IP_GROUP, IP_START);
        this.clientListener = clientListener;
        usersTracer = new ClientUsersTracer(clientListener);
        clientWatcher = new ClientWatcher(this, clientListener);

        clientListener.log("## Client running on port " + port + ", login is " + login);
    }

    public String getDirectory() {
        return directory;
    }

    public String getLogin() {
        return login;
    }

    public InetAddress getIP() {
        return addresIP;
    }

    public ClientUsersTracer getUsersTracer() {
        return usersTracer;
    }

    public void start() throws IOException {
        sendLogin(login);
        serverSocket = new ServerSocket(port, 256, addresIP);
        acceptingThread = new Thread(new ClientAccepter(serverSocket, this, clientListener));
        acceptingThread.start();
    }

    public void stop() {
        acceptingThread.interrupt();
        sendLogout(login);
    }

    public void sendLogin(String login) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending login request");
                stream.writeInt(LOGIN_COMMAND);
                stream.writeUTF(login);
            }
        });
    }

    public void sendLogout(String login) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending logout request");
                stream.writeInt(LOGOUT_COMMAND);
                stream.writeUTF(login);
            }
        });
    }

    public void sendFileCheck(String relativePath) {
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();

        if(file.isDirectory())
            return;

        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending file advertisement for " + relativePath);
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
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending send request for file " + relativePath);
                stream.writeInt(NEED_FILE_COMMAND);
                stream.writeUTF(relativePath);
            }
        });
    }

    public void sendFileDelete(String relativePath) {
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                clientListener.log("<< sending delete request for file " + relativePath);
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
                    clientListener.log("<< sending file " + relativePath);
                    stream.writeInt(SEND_FILE_COMMAND);
                }
                else {
                    clientListener.log("<< sending file " + relativePath + " to " + login);
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
        clientListener.log("## Checking if file " + relativePath + " is up to date");
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();
        if(modificationTime < otherModificationTime)
            return false;
        else
            return true;
    }

    public void deleteFile(String relativePath) {
        clientListener.log("## Deleting file " + relativePath);
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
                    clientListener.errorOccured();
                }
            });
        }

        abstract void send(DataOutputStream stream) throws IOException;
    }
}
