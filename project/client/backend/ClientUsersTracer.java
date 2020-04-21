package project.client.backend;

import java.util.*;

public class ClientUsersTracer {

    private LinkedList<String> activeClients = new LinkedList<>();

    public synchronized void addActiveUser(String login) {
        if(!activeClients.contains(login))
            activeClients.add(login);
        System.out.println(this.toString());
    }

    public synchronized  void removeActiveUser(String login) {
        activeClients.remove(login);
        System.out.println(this.toString());
    }

    public synchronized String[] getActiveUsersList() {
        String[] active = new String[activeClients.size()];
        activeClients.toArray(active);
        return active;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClientActiveUsersTracer[users: ");
        String[] activeUsers = getActiveUsersList();
        for(String user : activeUsers)
            builder.append(user + " ");
        builder.append("]");
        return builder.toString();
    }
}
