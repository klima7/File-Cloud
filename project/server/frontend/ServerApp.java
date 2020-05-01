package project.server.frontend;

// importy
import javafx.fxml.*;
import javafx.scene.*;
import project.server.backend.*;
import java.net.InetAddress;
import javafx.application.*;
import javafx.stage.*;
import java.io.*;
import java.util.*;
import static project.common.ImportantConstants.*;

/**
 * Główna klasa serwera, zawiera metodę main.
 * @author Łukasz Klimkiewicz
 */
public class ServerApp extends Application {

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

    /**
     * Metoda uruchamia serwer oraz pokazuje interfejs graficzny.
     * @param primaryStage Objekt sceny stworzony przez JavaFX.
     * @throws Exception gdy wystąpi błąd JavaFX.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ustalenie ścieżki do katalogu serwera
        String serverDirectory = null;
        List<String> parameters = getParameters().getRaw();
        if(parameters.size()>0)
            serverDirectory = parameters.get(0);
        else {
            File directory = askForDirectory();
            if(directory==null)
                System.exit(0);
            serverDirectory = directory.getAbsolutePath();
        }

        // Załadowanie schematu interfejsu użytkownika
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ServerLayout.fxml"));
        Parent root = loader.load();
        ServerLayoutController controller = loader.getController();

        // Uruchomienie backendu
        ServerBackend backend = new ServerBackend(serverDirectory, PORT, InetAddress.getByName(SERVER_ADDRESS), new ServerHandler(controller));
        backend.startServer();
        controller.setDirectoryPath(serverDirectory);

        // Ustawienie akcji wykonywanych przy zamykaniu okna
        primaryStage.setOnCloseRequest((WindowEvent event) -> { backend.stopServer(); System.exit(0); });

        // Ustalenie wymiarów okna
        primaryStage.setTitle(TITLE);
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        // Ustawienie sceny i pokazanie GUI
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    // Wyświetla okno wyboru katalogu
    private File askForDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Server Directory");
        return chooser.showDialog(null);
    }

    /**
     * Metoda uruchamia mechanizm JavaFX.
     * @param args argumenty, czyli ścieżka do katalogu serwera.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
