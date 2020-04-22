package project.server.frontend;

import project.server.backend.*;
import javafx.application.*;

public class ServerHandler implements ServerListener {

    private ServerLayoutController controller;

    public ServerHandler(ServerLayoutController controller) {
        this.controller = controller;
    }

    public void userLoggedIn(String username, String directoryPath) {
        System.out.println("===================== User logged in =====================");
        Platform.runLater(() -> controller.addTab(username, directoryPath));
    }

    public void userLoggedOut(String username) {
        System.out.println("===================== User logged out =====================");
        Platform.runLater(() -> controller.removeTab(username));
    }

    public void filesUpdated(String username) {
        System.out.println("===================== Updating files in directory =====================");
        Platform.runLater(() -> controller.updateTab(username));
    }

    public void log(String message) {
        System.out.println("===================== Logging =====================");
        Platform.runLater(() -> controller.addLog(message));
    }

    public void errorOccured(String message) {

    }

}
