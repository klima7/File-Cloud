package project.client.frontend;

import project.client.backend.*;
import javafx.fxml.*;
import javafx.scene.*;
import static project.common.Constants.*;
import java.io.*;
import java.util.concurrent.*;
import javafx.application.*;
import javafx.stage.*;

public class ClientApp extends Application {

    private static ClientBackend backend;

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ClientLayout.fxml"));
        Parent root = loader.load();
        ClientLayoutController controller = loader.getController();
        controller.set(backend.getLogin(), backend.getDirectory(), backend);

        backend.setClientListener(new ClientHandler(controller));
        backend.start();

        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            System.out.println("=================== STOPPING ================");
            backend.stop();
        });

        primaryStage.setTitle("PO2 Project Client");
        primaryStage.setWidth(680);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(680);
        primaryStage.setMinHeight(500);
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length<2) {
            System.err.println("Invalid arguments count");
            System.exit(1);
        }

        backend = new ClientBackend(args[0], args[1], PORT);
        launch();
        TimeUnit.DAYS.sleep(1);
    }
}
