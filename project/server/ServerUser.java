package project.server;

import java.io.*;
import java.util.*;

public class ServerUser {

    private String login;
    private String directory;
    private List<ServerClient> clients = new LinkedList();

    public ServerUser(String login, String rootDirectory) {
        this.login = login;
        this.directory = rootDirectory + "/" + login;

        File file = new File(directory);
        if(!file.exists())
            file.mkdir();
    }

    public String getDirectory() {
        return directory;
    }

    public String getLogin() {
        return login;
    }

    public void registerClient(ServerClient client) {
        clients.add(client);
    }

    public void unregisterClient(ServerClient client) {
        clients.remove(client);
    }

    public int getClientCount() {
        return clients.size();
    }

    public boolean deleteFile(String relativePath) {
        File file = new File(directory, relativePath);
        boolean deleted = file.delete();
        return deleted;
    }

    public File createFile(String relativePath) throws IOException {
        File newFile = new File(directory, relativePath);
        if (newFile.exists())
            newFile.delete();
        newFile.createNewFile();
        return newFile;
    }

    public boolean checkFile(String relativePath, long compareTime) {
        File file = new File(directory, relativePath);
        long modificationTime = file.lastModified();
        if(modificationTime < compareTime)
            return false;
        return true;
    }

    public void sendFileExcept(String relativePath, ServerClient except) {
        for(ServerClient client : clients) {
            if(client != except)
                client.sendFile(relativePath);
        }
    }

    public void sendFileEveryone(String relativePath) {
        for(ServerClient client : clients)
            client.sendFile(relativePath);
    }

    public void sendDeleteExcept(String relativePath, ServerClient except) {
        for(ServerClient client : clients) {
            if(client!=except)
                client.sendDelete(relativePath);
        }
    }

    public void sendDeleteEveryone(String relativePath) {
        for(ServerClient client : clients)
            client.sendDelete(relativePath);
    }

    public void sendActiveUserEveryone(String login) {
        for(ServerClient client : clients)
            client.sendUserActive(login);
    }

    public void sendInactiveUserEveryone(String login) {
        for(ServerClient client : clients)
            client.sendUserInactive(login);
    }

    public void receiveFileData(String relativePath, long modificationTime, long size, DataInputStream input) throws IOException{
        File newFile = createFile(relativePath);

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
    }
}
