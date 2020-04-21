package project.server.frontend;

import javafx.fxml.*;
import javafx.scene.control.*;

import java.io.IOException;

public class ServerLayoutController {

    @FXML
    private ListView logList;

    @FXML
    private ScrollPane logScrollPane;

    @FXML
    private TabPane tabPane;

    public void initialize() {
        for(int i=0; i<100; i++)
            addLog(""+i);
        addTab().setTitle("klima7");
        addTab();
    }

    public void addLog(String message) {
        logList.getItems().add(0, message);
    }

    public ServerTabController addTab() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ServerTab.fxml"));
        Tab tab = null;
        try {
            tab = loader.load();
        } catch(IOException exception) {}

        tabPane.getTabs().add(tab);
        return loader.getController();
    }
}
