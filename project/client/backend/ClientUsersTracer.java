package project.client.backend;

import java.util.*;

public class ClientUsersTracer {

    private LinkedList<String> activeClients = new LinkedList<>();
    private ClientListener clientListener;

    public void setClientListener(ClientListener clientListener) {
        this.clientListener = clientListener;
    }

    public synchronized void addActiveUser(String login) {
        if(!activeClients.contains(login))
            activeClients.add(login);
        System.out.println(this.toString());

        System.out.println("Here 1");
        if(clientListener!=null) {
            System.out.println("Here 2");
            clientListener.userLogginIn(login);
        }
    }

    public synchronized  void removeActiveUser(String login) {
        activeClients.remove(login);
        System.out.println(this.toString());

        if(clientListener!=null)
            clientListener.userLogginOut(login);
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
