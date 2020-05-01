package project.client.frontend;

import javafx.scene.control.*;
import project.client.backend.*;
import javafx.fxml.*;
import javafx.scene.*;
import project.common.*;
import javafx.application.*;
import javafx.stage.*;
import java.io.*;
import java.util.*;

/**
 * Główna klasa klienta, zawiera metodę main.
 */
public class ClientApp extends Application {

    /** Tytuł okna */
    public static final String TITLE = "PO2 Project Client";
    /** Szerokość okna */
    public static final int WIDTH = 680;
    /** Wysokość okna */
    public static final int HEIGHT = 800;
    /** Minimalna szerokość okna */
    public static final int MIN_WIDTH = 680;
    /** Minimalna wysokość okna */
    public static final int MIN_HEIGHT = 500;
    /** Backend klienta */
    private static ClientBackend backend;

    /**
     * Metoda uruchamia backend klienta oraz Wyświetla interfejs graficzny.
     * @param primaryStage Obiekt Sceny tworzony przez JavaFX.
     * @throws Exception Dowolny wyjątek zgłoszony przez JavaFX.
     */
    public void start(Stage primaryStage) throws Exception {
        String login = null;
        String directory = null;

        // Gdy podano wszystkie argumenty
        List<String> args = getParameters().getRaw();
        if(args.size()>=2) {
            login = args.get(0);
            directory = args.get(1);
        }

        // Gdy nie podano ścieżki do katalogu lokalnego
        else if(args.size()==1) {
            login = args.get(0);
            File file = askForDirectory();
            if(file==null) System.exit(0);
            directory = file.getAbsolutePath();
        }

        // Gdy nie podano ani loginu ani ścieżki
        else if(args.size()==0) {
            login = askForLogin();
            if(login==null) System.exit(0);
            File file = askForDirectory();
            if(file==null) System.exit(0);
            directory = file.getAbsolutePath();
        }

        // Wczytanie interfejsu graficznego z pliku FXML
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ClientLayout.fxml"));
        Parent root = loader.load();
        ClientLayoutController controller = loader.getController();

        // Stworzenie i uruchomienie backendu
        backend = new ClientBackend(login, directory, ImportantConstants.PORT, new ClientHandler(controller));
        try { backend.start(); }
        catch(IOException e) { System.err.println("This port numer is probably in use!"); System.exit(1); }

        // Dokończenie inicjalizacji kontrolera
        controller.setBackend(backend);

        // Zakończenie pracy backendu w przypadku zamknięcia frontendu
        primaryStage.setOnCloseRequest((WindowEvent event) -> backend.stop());

        // Ustalenie parametrów okna
        primaryStage.setTitle(TITLE);
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        // Ustawienie zawartości okna i pokazanie go
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    // Wyświetla okno wyboru katalogu
    private File askForDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select you local directory:");
        return chooser.showDialog(null);
    }

    // Wyświetla proźbę o podanie loginu
    private String askForLogin() {
        TextInputDialog dialog = new TextInputDialog("Login");

        dialog.setTitle("Login");
        dialog.setHeaderText("Enter your login:");
        dialog.setContentText("Login:");

        Optional<String> result = dialog.showAndWait();
        if(result.isPresent())
            return result.get();
        else
            return null;
    }

    /**
     * Metoda uruchamia mechanizm JavaFX.
     * @param args Argumenty, czyli login użytkownika oraz ścieżka do katalogu lokalnego.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
