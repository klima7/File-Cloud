package project.server.frontend;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import project.common.*;
import java.io.*;

/**
 * Klasa jest kontrolerem JavaFX karty odpowiadającej pojedyńczemu użytkownikowi.
 */
public class ServerTabController {

    // Karta odpowiadająca użytkownikowi
    @FXML
    private Tab tab;

    // Tabela zawierająca listę plików w katalogu użytkownika
    @FXML
    private TableView table;

    // Login użytkownika wyświetlany jako tytuł karty
    private String login;

    // Ścieżka do katalogu, którego zawartość jest wyświetlana w karcie
    private String directoryPath;

    /**
     * Metoda ustala login użytkownika, który jest wyświetlany w tytule karty.
     * @param login Login użytkownika.
     */
    public void setLogin(String login) {
        this.login = login;
        tab.setText(login);
    }

    /**
     * Metoda ustala ścieżkę do katalogu, którego zawartość jest wyświetlana.
     * @param directoryPath Ścieżka do katalogu użytkownika.
     */
    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
        updateFilesList();
    }

    /**
     * Metoda zwraca login użytkownika, który wyświetla się na karcie.
     * @return Login użytkownika.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Metoda zwraca ścieżkę do katalogu, którego zawartość jest wyświetlana w karcie.
     * @return Ścieżka do katalogu użytkownika.
     */
    public String getDirectoryPath() {
        return directoryPath;
    }

    /**
     * Metoda aktualizuje wyświetlaną listę plików.
     */
    public void updateFilesList() {
        ObservableList items = table.getItems();
        items.clear();

        for(File file : new File(directoryPath).listFiles()) {
            String filename = file.getName();
            long size = file.length();
            items.add(new FileModel(filename, size, FileModel.convertToReadableTime(file.lastModified())));
        }
    }
}
