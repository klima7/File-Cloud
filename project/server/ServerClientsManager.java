package project.server;

import java.util.*;
import java.net.*;

public class ServerClientsManager {

    private ServerBackend serverBackend;
    private Map<InetAddress, ServerClient> clients = new HashMap<>();
    private Map<String, ServerUser> users = new HashMap<>();

    public ServerClientsManager(ServerBackend serverBackend) {
        this.serverBackend = serverBackend;
    }

    public void addClient(InetAddress address, String login) {
        ServerUser user = users.get(login);

        if(user==null) {
            sendUserActiveEveryone(login);
            user = new ServerUser(login, serverBackend.getRootDirectory());
            users.put(login, user);
        }

        ServerClient newClient = new ServerClient(address, user, serverBackend.getPort());
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
}
