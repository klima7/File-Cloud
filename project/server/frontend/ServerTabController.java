package project.server.frontend;

import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.control.*;
import project.common.*;
import java.io.*;

public class ServerTabController {
    @FXML
    private TableView table;

    @FXML
    private Tab tab;

    private String login;
    private String directoryPath;

    public void initialize() {
        table.setSelectionModel(null);
    }

    public String getLogin() {
        return login;
    }

    public void set(String login, String directoryPath) {
        this.login = login;
        this.directoryPath = directoryPath;

        tab.setText(login);
        updateFilesList();
    }

    public void updateFilesList() {
        ObservableList items = table.getItems();
        items.clear();

        for(File file : new File(directoryPath).listFiles()) {
            String filename = file.getName();
            long size = file.length();
            long modification = file.lastModified();
            items.add(new FileModel(filename, size, modification));
        }
    }
}
