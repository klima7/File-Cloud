package project.server.frontend;

import javafx.fxml.*;
import javafx.scene.control.*;

public class ServerTabController {
    @FXML
    private TableView table;

    @FXML
    private Tab tab;

    public void setTitle(String title) {
        tab.setText(title);
    }
}
