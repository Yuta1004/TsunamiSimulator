import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            // Scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UI.fxml"));
            loader.setController(new UIController());
            Scene scene = new Scene(loader.load());

            // Stage
            stage.setTitle("Tsunami Simulator Java Edetion");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
