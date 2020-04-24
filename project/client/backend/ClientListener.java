package project.client.backend;

public interface ClientListener {

    void filesUpdated();
    void log(String message);
    void errorOccured();
    void userLogginIn(String login);
    void userLogginOut(String login);

}
