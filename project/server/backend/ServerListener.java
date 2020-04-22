package project.server.backend;

public interface ServerListener {

    void userLoggedIn(String username, String directoryPath);
    void userLoggedOut(String username);
    void filesUpdated(String username);
    void log(String message);
    void errorOccured(String message);

}
