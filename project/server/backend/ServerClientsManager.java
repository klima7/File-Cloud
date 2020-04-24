package project.server.backend;

import java.util.*;
import java.net.*;

public class ServerClientsManager {

    private String rootDirectory;
    private int port;
    private ServerListener serverListener;

    private Map<InetAddress, ServerClient> clients = new HashMap<>();
    private Map<String, ServerUser> users = new HashMap<>();

    public ServerClientsManager(String rootDirectory, int port, ServerListener serverListener) {
        this.rootDirectory = rootDirectory;
        this.port = port;
        this.serverListener = serverListener;
    }

    public void addClient(InetAddress address, String login) {
        ServerUser user = users.get(login);

        if(user==null) {
            sendUserActiveEveryone(login);
            user = new ServerUser(login, rootDirectory, serverListener);
            users.put(login, user);

            serverListener.userLoggedIn(user.getLogin(), user.getDirectory());
            serverListener.log("# User " + login + " joined");
        }

        ServerClient newClient = new ServerClient(address, user, port, serverListener);
        clients.put(address, newClient);
        user.registerClient(newClient);

        sendAllActiveUsersToClient(newClient);
    }

    public void removeClient(InetAddress address) {
        ServerClient client = clients.remove(address);
        client.logout();
        ServerUser user = client.getUser();
        user.unregisterClient(client);
        if(user.getClientCount()==0) {
            users.remove(user);
            sendUserInactiveEveryone(user.getLogin());
            serverListener.userLoggedOut(user.getLogin());
            serverListener.log("# User " + user.getLogin() + " left");
        }
    }

    public ServerClient getClient(InetAddress address) {
        return clients.get(address);
    }

    public ServerUser getUser(String login) {
        return users.get(login);
    }

    public void sendUserActiveEveryone(String login) {
        for(ServerUser user : users.values())
            user.sendActiveUserEveryone(login);
    }

    public void sendUserInactiveEveryone(String login) {
        for(ServerUser user : users.values())
            user.sendInactiveUserEveryone(login);
    }

    public void sendAllActiveUsersToClient(ServerClient client) {
        for(ServerUser user : users.values())
            client.sendUserActive(user.getLogin());
    }

    public void sendServerDownEveryone() {
        for(ServerUser user : users.values())
            user.sendServerDownEveryone();
    }
}
