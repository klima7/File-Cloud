package project.client.frontend;

import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import project.client.backend.ClientBackend;
import project.common.*;
import java.io.*;
import java.util.*;

public class ClientLayoutController {

    @FXML
    private ListView logList;

    @FXML
    private ListView usersList;

    @FXML
    private Menu loginMenu;

    @FXML
    private TableView table;

    private String login;
    private String directoryPath;
    private ClientBackend backend;

    public void initialize() {
        logList.setSelectionModel(null);
    }

    public void set(String login, String directoryPath, ClientBackend backend) {
        this.login = login;
        this.directoryPath = directoryPath;
        this.backend = backend;

        loginMenu.setText("Your login: " + login);
        updateFilesList();
    }

    public void addLog(String message) {
        GregorianCalendar now = new GregorianCalendar();
        String prefix = "[" + now.get(GregorianCalendar.HOUR) + ":" + now.get(GregorianCalendar.MINUTE) + ":" +
                now.get(GregorianCalendar.SECOND) + "." + now.get(GregorianCalendar.MILLISECOND) + "] ";

        logList.getItems().add(0, String.format("%-30s %s", prefix, message));
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

    public void addUser(String login) {
        if(!usersList.getItems().contains(login))
            usersList.getItems().add(login);
    }

    public void removeUser(String login) {
        usersList.getItems().remove(login);
    }

    public void sendFileToUser(ActionEvent event) {
        System.out.println("=============== Sending file to user ==================");

        List<String> selectedUsers = usersList.getSelectionModel().getSelectedItems();
        ObservableList<FileModel> selectedFiles = table.getSelectionModel().getSelectedItems();

        if(selectedUsers.size()==0 || selectedFiles.size()==0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Unable to send");
            alert.setHeaderText("");
            alert.setContentText("You must select both File which you want to send and target user from the lists above");
            alert.showAndWait();
            return;
        }

        String filename = selectedFiles.get(0).getFilename();
        String username = selectedUsers.get(0);

        backend.sendFileData(filename, username);

        usersList.getSelectionModel().clearSelection();
        table.getSelectionModel().clearSelection();
    }


}
