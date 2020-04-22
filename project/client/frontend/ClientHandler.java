package project.client.frontend;

import javafx.application.*;
import project.client.backend.*;

public class ClientHandler implements ClientListener {

    public ClientLayoutController controller;

    public ClientHandler(ClientLayoutController controller) {
        this.controller = controller;
    }

    public void filesUpdated() {
        System.out.println("===================== Updating files in directory =====================");
        Platform.runLater(() -> controller.updateFilesList());
    }

    public void log(String message) {
        System.out.println("===================== Logging =====================");
        Platform.runLater(() -> controller.addLog(message));
    }

    public void errorOccured(String message) {

    }

    public void userLogginIn(String login) {
        System.out.println("===================== User logged in =====================");
        Platform.runLater(() -> controller.addUser(login));
    }

    public void userLogginOut(String login) {
        System.out.println("===================== User logged out =====================");
        Platform.runLater(() -> controller.removeUser(login));
    }
}
