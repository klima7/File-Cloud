package project.server.backend;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import static project.common.Constants.*;

public class ServerClient {
    private InetAddress addressIP;
    private int port;
    private ServerUser user;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public ServerClient(InetAddress addressIP, ServerUser user, int port) {
        this.addressIP = addressIP;
        this.port = port;
        this.user = user;
    }

    public ServerUser getUser() {
        return user;
    }

    public InetAddress getIpAddress() {
        return addressIP;
    }

    public void logout() {
        executor.shutdown();
    }

    public void sendLoginSuccess() {
        System.out.println(">> sending login success");
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(LOGIN_SUCCESS_COMMAND);
            }
        });
    }

    public void sendFile(String relativePath) {
        System.out.println(">> sending file " + relativePath);
        File file = new File(user.getDirectory(), relativePath);
        long modificationTime = file.lastModified();
        long size = file.length();

        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(SEND_FILE_COMMAND);
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

    public void sendAdvertisement(String relativePath) {
        System.out.println(">> sending check " + relativePath);
        File file = new File(user.getDirectory(), relativePath);
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

    public void sendAdvertisements() {
        String[] list = new File(user.getDirectory()).list();
        for(String name : list)
            sendAdvertisement(name);
    }

    public void sendRequest(String relativePath) {
        System.out.println(">> sending need " + relativePath);
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(NEED_FILE_COMMAND);
                stream.writeUTF(relativePath);
            }
        });
    }

    public void sendDelete(String relativePath) {
        System.out.println(">> sending delete " + relativePath);
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(DELETE_FILE_COMMAND);
                stream.writeUTF(relativePath);
            }
        });
    }

    public void sendUserActive(String login) {
        System.out.println(">> sending active " + login);
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(USER_ACTIVE_COMMAND);
                stream.writeUTF(login);
            }
        });
    }

    public void sendUserInactive(String login) {
        System.out.println(">> sending active " + login);
        executor.execute(new SendWrapper() {
            void send(DataOutputStream stream) throws IOException {
                stream.writeInt(USER_INACTIVE_COMMAND);
                stream.writeUTF(login);
            }
        });
    }

    private abstract class SendWrapper implements Runnable {
        public void run() {
            executor.execute(() ->
            {
                try (Socket socket = new Socket(addressIP, port)) {
                    DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                    send(stream);
                } catch (IOException e) {
                    System.err.println("Error occured while sending");
                    e.printStackTrace();
                }
            });
        }

        abstract void send(DataOutputStream stream) throws IOException;
    }

}
