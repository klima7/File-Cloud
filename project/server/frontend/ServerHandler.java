package project.server.frontend;

import project.server.backend.*;
import javafx.application.*;

public class ServerHandler implements ServerListener {

    private ServerLayoutController controller;

    public ServerHandler(ServerLayoutController controller) {
        this.controller = controller;
    }

    public void userLoggedIn(String username, String directoryPath) {
        Platform.runLater(() -> controller.addTab(username, directoryPath));
    }

    public void userLoggedOut(String username) {
        Platform.runLater(() -> controller.removeTab(username));
    }

    public void filesUpdated(String username) {
        Platform.runLater(() -> controller.updateTab(username));
    }

    public void log(String message) {
        Platform.runLater(() -> controller.addLog(message));
    }

    public void errorOccured(String message) {

    }

}
