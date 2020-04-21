package project.server.frontend;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import project.server.backend.ServerBackend;

import static project.common.Constants.*;
import java.io.*;
import java.util.concurrent.*;
import javafx.application.*;
import javafx.stage.*;

public class ServerApp extends Application {

    public static final String SERVER_DIRECTORY = "/home/klima7/SERVER";
    private static ServerBackend backend;

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ServerLayout.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Client");
        primaryStage.setWidth(600);
        primaryStage.setHeight(800);
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        backend = new ServerBackend(SERVER_DIRECTORY, PORT);
        backend.startServer();
        launch();
        TimeUnit.DAYS.sleep(1);
    }
}
