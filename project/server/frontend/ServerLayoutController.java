package project.server.frontend;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import java.awt.Desktop;
import java.io.*;
import java.util.*;

/**
 * Klasa jest kontrolerem interfejsu użytkownika zdefiniwanego w pliku FXML "ServerLayout"
 */
public class ServerLayoutController {

    // Dolny panel interfejsu zawierający logi
    @FXML
    private ListView logList;

    // Panel mogący zawierać karty poszczególnych użytkowników
    @FXML
    private TabPane tabPane;

    // Nieklikalne menu wyświetlające liczbę aktualnie aktywnych użytkowników
    @FXML
    private Menu countMenu;

    // Lista wszystkich kontrolerów kart. Każdemu zalogowanemu użytkownikowi odpowiada jedna karta
    private LinkedList<ServerTabController> tabControllers = new LinkedList<>();

    // Ścieżka do katalogu serwera. Jest potrzeba dla przycisku "open directory"
    private String directoryPath;

    /**
     * Metoda ustala ścieżkę do katalogu, który jest otwierany po wciśnięciu przycisku "Open directory".
     * @param directoryPath Ścieżka do katalogu serwera.
     */
    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Metoda zwraca ścieżkę do katalogu, który jest otwierany po wciśnięciu przycisku "Open directory".
     * @return Ścieżka do katalogu serwera.
     */
    public String getDirectoryPath() {
        return directoryPath;
    }

    /**
     * Metoda wyświetla wiadomość poprzedzoną aktualnym czasem w panelu z logami
     * @param message Treść wiadomości.
     */
    public void addLog(String message) {
        GregorianCalendar now = new GregorianCalendar();
        String prefix = "[" + now.get(GregorianCalendar.HOUR) + ":" + now.get(GregorianCalendar.MINUTE) + ":" +
                now.get(GregorianCalendar.SECOND) + "] ";

        logList.getItems().add(0, String.format("%s  %s", prefix, message));
    }

    /**
     * Metoda dodaje nową kartę odpowiadającą użytkownikowi o podanemu loginie i wyświetlającą zawartość
     * katalogu podanego za pomocą ścieżki.
     * @param login Login użytkownika.
     * @param directoryPath Ścieżka do katalogu użytkownika.
     */
    public void addTab(String login, String directoryPath) {
        // Wczytanie układu graficznego karty
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ServerTab.fxml"));
        Tab tab = null;
        try { tab = loader.load(); }
        // Wysoce nieprawdopodobny błąd
        catch(IOException e) { e.printStackTrace(); }

        // Odpowiednie skonfigurowanie karty
        tabPane.getTabs().add(tab);
        ServerTabController tabController = loader.getController();
        tabController.setLogin(login);
        tabController.setDirectoryPath(directoryPath);
        tabControllers.add(tabController);

        // Zwiększenie liczby aktywnych użytkowników
        countMenu.setText("Active Users: " + tabControllers.size());
    }

    /**
     * Metoda usówa kartę odpowiadającą danemu użytkownikowi oraz zmiejsza licznik aktywnym użytkowników o 1.
     * Jeżeli nie istnieje karta odpowiadająca podanemu użytkownikowi to metoda nie ma żadnych efektów.
     * @param login Login użytkownika.
     */
    public void removeTab(String login) {
        // Usuwanie karty użytkownika
        boolean found = false;
        ObservableList<Tab> tabs = tabPane.getTabs();
        for(Tab tab : tabPane.getTabs()) {
            if(tab.getText().equals(login)) {
                tabs.remove(tab);
                found = true;
                break;
            }
        }

        // Jeżeli nie znaleziono karty odpowiadającej danemu użytkownikowi to zakończ
        if(!found)
            return;

        // Usuwanie kontrolera użytkownika
        for(ServerTabController controller : tabControllers) {
            if(controller.getLogin().equals(login)) {
                tabControllers.remove(controller);
                break;
            }
        }

        // Zmniejszenie liczby aktywnym użytkowników
        countMenu.setText("Active Users: " + tabControllers.size());
    }

    /**
     * Metoda aktualizuje listę plików danego użytkownika.
     * @param login Login użytkownika.
     */
    public void updateTab(String login) {
        for(ServerTabController controller : tabControllers) {
            if(controller.getLogin().equals(login)) {
                controller.updateFilesList();
                break;
            }
        }
    }

    /**
     * Metoda w odpowiedzi na kliknięcie przycisku otwiera katalog serwera za pomocą domyślnego eksploratora plików.
     * @param event Zdarzenie pliknięcia przycisku.
     */
    public void openDirectory(MouseEvent event) {
        addLog("## Opening server directory");
        new Thread(() -> {
            try {
                Desktop desktop = Desktop.getDesktop();
                File file = new File(directoryPath);
                desktop.open(file);
            } catch(IOException e) {
                addLog("## Unable to open file explorer");
            }
        }).start();
    }
}
