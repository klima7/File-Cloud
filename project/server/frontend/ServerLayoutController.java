package project.server.frontend;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import project.common.*;
import project.server.backend.*;
import java.awt.Desktop;
import java.io.*;
import java.util.*;

public class ServerLayoutController {

    @FXML
    private ListView logList;

    @FXML
    private TabPane tabPane;

    @FXML
    private Menu countMenu;

    private LinkedList<ServerTabController> tabControllers = new LinkedList<>();
    private ServerBackend backend;

    public void set(ServerBackend backend) {
        this.backend = backend;
    }

    public void addLog(String message) {
        GregorianCalendar now = new GregorianCalendar();
        String prefix = "[" + now.get(GregorianCalendar.HOUR) + ":" + now.get(GregorianCalendar.MINUTE) + ":" +
                now.get(GregorianCalendar.SECOND) + "] ";

        logList.getItems().add(0, String.format("%s  %s", prefix, message));
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
        countMenu.setText("Active Users: " + tabControllers.size());
    }

    public void removeTab(String login) {
        ObservableList<Tab> tabs = tabPane.getTabs();
        for(Tab tab : tabPane.getTabs()) {
            if(tab.getText().equals(login)) {
                tabs.remove(tab);
                break;
            }
        }

        for(ServerTabController controller : tabControllers) {
            if(controller.getLogin().equals(login)) {
                tabControllers.remove(controller);
                break;
            }
        }

        countMenu.setText("Active Users: " + tabControllers.size());
    }

    public void updateTab(String login) {
        for(ServerTabController controller : tabControllers) {
            if(controller.getLogin().equals(login)) {
                controller.updateFilesList();
                break;
            }
        }
    }

    public void openDirectory(MouseEvent event) {
        addLog("## Opening server directory");
        new Thread(() -> {
            try {
                Desktop desktop = Desktop.getDesktop();
                File file = new File(backend.getRootDirectory());
                desktop.open(file);
            } catch(IOException e) {
                addLog("## Unable to open file explorer");
            }
        }).start();
    }
}
