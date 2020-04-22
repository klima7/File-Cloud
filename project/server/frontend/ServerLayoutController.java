package project.server.frontend;

import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.control.*;
import java.io.*;
import java.util.*;

public class ServerLayoutController {

    @FXML
    private ListView logList;

    @FXML
    private TabPane tabPane;

    private LinkedList<ServerTabController> tabControllers = new LinkedList<>();

    public void initialize() {
        logList.setSelectionModel(null);
    }

    public void addLog(String message) {
        GregorianCalendar now = new GregorianCalendar();
        String prefix = "[" + now.get(GregorianCalendar.HOUR) + ":" + now.get(GregorianCalendar.MINUTE) + ":" +
                now.get(GregorianCalendar.SECOND) + "." + now.get(GregorianCalendar.MILLISECOND) + "] ";

        logList.getItems().add(0, String.format("%-30s %s", prefix, message));
    }

    public void addTab(String login, String directoryPath) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ServerTab.fxml"));
        Tab tab = null;
        try {
            tab = loader.load();
        } catch(IOException exception) {}

        tabPane.getTabs().add(tab);
        ServerTabController tabController = loader.getController();
        tabController.set(login, directoryPath);

        tabControllers.add(tabController);
    }

    public void removeTab(String login) {
        ObservableList<Tab> tabs = tabPane.getTabs();
        for(Tab tab : tabPane.getTabs()) {
            if(tab.getText().equals(login)) {
                tabs.remove(tab);
                break;
            }
        }
    }

    public void updateTab(String login) {
        for(ServerTabController controller : tabControllers) {
            if(controller.getLogin().equals(login)) {
                controller.updateFilesList();
                break;
            }
        }
    }
}
