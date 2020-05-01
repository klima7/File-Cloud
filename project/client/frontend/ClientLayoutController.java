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

/**
 * Kontroler interfejsu JavaFX klienta. Umożliwia wykonywanie operacji na interfejsie użytkownika.
 */
public class ClientLayoutController {

    // Elementy interfejsu graficznego, których nazwy są samoopisowe
    @FXML
    private ListView logList;
    @FXML
    private ListView usersList;
    @FXML
    private Menu loginMenu;
    @FXML
    private Menu addressMenu;
    @FXML
    private TableView fileTable;

    // Backend wykorzystywany do poznania loginu, adresu IP i ścieżki do katalogu lokalnego
    private ClientBackend backend;

    /**
     * Metoda wykorzystuje przekazany objekt backendu, by poznać login użytkownika, jego adres IP oraz ścieżkę
     * do katalogu i wyświetlić te informacje w interfejscie użytkownika.
     * @param backend backend klienta.
     */
    public void setBackend(ClientBackend backend) {
        this.backend = backend;

        loginMenu.setText("Your login: " + backend.getLogin());
        addressMenu.setText("Your IP is: " + backend.getIP().getHostName());
        updateFilesList();
    }

    /**
     * Metoda wyświetla wiadomości w polu z logami.
     * @param message Treść wiadomości.
     */
    public void addLog(String message) {
        GregorianCalendar now = new GregorianCalendar();
        String prefix = "[" + now.get(GregorianCalendar.HOUR) + ":" + now.get(GregorianCalendar.MINUTE) + ":" +
                now.get(GregorianCalendar.SECOND) + "] ";

        logList.getItems().add(0, String.format("%s  %s", prefix, message));
    }

    /**
     * Metoda aktualizuję listę wyświetlanych plików w katalogu użytkownika.
     */
    public void updateFilesList() {
        ObservableList items = fileTable.getItems();
        items.clear();

        for(File file : new File(backend.getDirectory()).listFiles()) {
            String filename = file.getName();
            long size = file.length();

            items.add(new FileModel(filename, size, FileModel.convertToReadableTime(file.lastModified())));
        }
    }

    /**
     * Metoda dodaje użytkownika do listy aktywnych użytkowników.
     * @param login Login dodawanego użytkownika.
     */
    public void addUser(String login) {
        if(!usersList.getItems().contains(login))
            usersList.getItems().add(login);
    }

    /**
     * Metoda usówa użytkownika z listy aktywnych użytkowników.
     * @param login login usówanego użytkownika.
     */
    public void removeUser(String login) {
        usersList.getItems().remove(login);
    }

    /**
     * Metoda wysyłająca plik do innego użytkownika w odpowiedzi na wciśnięcie przycisku.
     * @param event Zdarzenie wciśnięcia przycisku.
     */
    public void sendFileToUser(ActionEvent event) {
        // Pobranie listy wybranych użytkowników i plików
        List<String> selectedUsers = usersList.getSelectionModel().getSelectedItems();
        ObservableList<FileModel> selectedFiles = fileTable.getSelectionModel().getSelectedItems();

        // Sprawdzenie, czy użytkownik i plik został wybrany
        if(selectedUsers.size()==0 || selectedFiles.size()==0) {
            addLog("!! You must select file and user before sending");
            return;
        }

        String filename = selectedFiles.get(0).getFilename();
        String username = selectedUsers.get(0);

        // Sprawdzenie czy użytkownik nie chce wysłąć pliku do samego siebie
        if(username.equals(backend.getLogin())) {
            addLog("!! You can't send file to yourself");
            return;
        }

        // Wysłanie pliku
        backend.sendFileData(filename, username);

        // Wyczyszczenie zaznaczenia
        usersList.getSelectionModel().clearSelection();
        fileTable.getSelectionModel().clearSelection();
    }

    /**
     * Metoda otwierająca katalog lokalny użytkownika w odpowiedzi na wciśnięci przycisku.
     * @param event Zdarzenie wciśnięcia przycisku.
     */
    public void openDirectory(MouseEvent event) {
        addLog("## Opening local directory");
        new Thread(() -> {
            try {
                File file = new File(backend.getDirectory());
                Desktop.getDesktop().open(file);
            } catch(IOException e) {
                addLog("## Unable to open file explorer");
            }
        }).start();
    }
}
