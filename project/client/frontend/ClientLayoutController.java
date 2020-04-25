package project.client.frontend;

import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.input.*;
import project.client.backend.*;
import project.common.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ClientLayoutController {

    @FXML
    private ListView logList;

    @FXML
    private ListView usersList;

    @FXML
    private Menu loginMenu;

    @FXML
    private Menu addressMenu;

    @FXML
    private TableView table;

    private ClientBackend backend;

    public void set(ClientBackend backend) {
        this.backend = backend;

        loginMenu.setText("Your login: " + backend.getLogin());
        addressMenu.setText("Your IP is: " + backend.getIP().getHostName());
        updateFilesList();
    }

    public void addLog(String message) {
        GregorianCalendar now = new GregorianCalendar();
        String prefix = "[" + now.get(GregorianCalendar.HOUR) + ":" + now.get(GregorianCalendar.MINUTE) + ":" +
                now.get(GregorianCalendar.SECOND) + "] ";

        logList.getItems().add(0, String.format("%s  %s", prefix, message));
    }

    public void updateFilesList() {
        ObservableList items = table.getItems();
        items.clear();

        for(File file : new File(backend.getDirectory()).listFiles()) {
            String filename = file.getName();
            long size = file.length();
            long modification = file.lastModified();
            items.add(new FileModel(filename, size, modification));
        }
    }

    public void addUser(String login) {
        if(!usersList.getItems().contains(login))
            usersList.getItems().add(login);
    }

    public void removeUser(String login) {
        usersList.getItems().remove(login);
    }

    public void sendFileToUser(ActionEvent event) {
        List<String> selectedUsers = usersList.getSelectionModel().getSelectedItems();
        ObservableList<FileModel> selectedFiles = table.getSelectionModel().getSelectedItems();

        if(selectedUsers.size()==0 || selectedFiles.size()==0) {
            addLog("!! You must select file and user before sending");
            return;
        }

        String filename = selectedFiles.get(0).getFilename();
        String username = selectedUsers.get(0);

        if(username.equals(backend.getLogin())) {
            addLog("!! You can't send file to yourself");
            return;
        }

        backend.sendFileData(filename, username);

        usersList.getSelectionModel().clearSelection();
        table.getSelectionModel().clearSelection();
    }

    public void openDirectory(MouseEvent event) {
        addLog("## Opening local directory");
        new Thread(() -> {
            try {
                Desktop desktop = Desktop.getDesktop();
                File file = new File(backend.getDirectory());
                desktop.open(file);
            } catch(IOException e) {
                addLog("## Unable to open file explorer");
            }
        }).start();
    }
}
