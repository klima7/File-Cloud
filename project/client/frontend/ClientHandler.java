package project.client.frontend;

import javafx.application.*;
import javafx.scene.control.*;
import project.client.backend.*;
import java.util.concurrent.*;

public class ClientHandler implements ClientListener {

    public ClientLayoutController controller;

    public ClientHandler(ClientLayoutController controller) {
        this.controller = controller;
    }

    public void filesUpdated() {
        Platform.runLater(() -> controller.updateFilesList());
    }

    public void log(String message) {
        Platform.runLater(() -> controller.addLog(message));
    }

    public void userLogginIn(String login) {
        Platform.runLater(() -> controller.addUser(login));
    }

    public void userLogginOut(String login) {
        Platform.runLater(() -> controller.removeUser(login));
    }

    public void errorOccured() {
        Platform.runLater(() -> {
            controller.addLog("## Connection failure, server is probably down. Quiting in 5 seconds");
        });

        try { TimeUnit.SECONDS.sleep(5); }
        catch(InterruptedException e) { e.printStackTrace(); }
        System.exit(0);
    }
}
