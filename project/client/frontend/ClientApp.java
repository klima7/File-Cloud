package project.client.frontend;

import project.client.backend.*;
import javafx.fxml.*;
import javafx.scene.*;
import project.common.*;
import javafx.application.*;
import javafx.stage.*;

import java.io.IOException;
import java.util.*;

/**
 * Główna klasa klienta, zawiera metodę main.
 */
public class ClientApp extends Application {

    /** Tytuł okna */
    public static final String TITLE = "PO2 Project Server";

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

        // Sprawdzenie, czy podaną odpowiednią liczbę argumentów
        List<String> args = getParameters().getRaw();
        if(args.size()<2) {
            System.err.println("Invalid arguments count");
            System.exit(1);
        }

        // Wczytanie interfejsu graficznego z pliku FXML
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ClientLayout.fxml"));
        Parent root = loader.load();
        ClientLayoutController controller = loader.getController();

        // Stworzenie i uruchomienie backendu
        backend = new ClientBackend(args.get(0), args.get(1), ImportantConstants.PORT, new ClientHandler(controller));
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

    /**
     * Metoda uruchamia mechanizm JavaFX.
     * @param args Argumenty, czyli login użytkownika oraz ścieżka do katalogu lokalnego.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
