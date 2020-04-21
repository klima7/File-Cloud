package project.server.backend;

import java.io.*;

public interface ServerListener {

    void userLoggedIn(String username, File userDirectory);
    void userLoggedOut(ServerUser user);
    void filesUpdated(String username, File userDirectory);
    void errorOccured(String message);

}
