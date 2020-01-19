import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;

import controller.MainUIController;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            // Window size
            Rectangle2D d = Screen.getPrimary().getVisualBounds();
            int width = (int)Math.min(1280, d.getWidth());
            int height = (int)Math.min(800, d.getHeight());

            // Scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainUI.fxml"));
            loader.setController(new MainUIController());
            Scene scene = new Scene(loader.load(), width, height);

            // Stage
            stage.setTitle("Tsunami Simulator Java Edition");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
