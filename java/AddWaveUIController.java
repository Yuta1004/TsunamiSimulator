import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class AddWaveUIController implements Initializable {

    @FXML
    Button okBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okBtn.setOnAction(event -> okBtn.getScene().getWindow().hide() );
    }

}