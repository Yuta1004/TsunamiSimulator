import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import lib.ArgsParser;
import controller.MainUIController;

public class Main extends Application {

    private static Locale locale;

    public static void main(String[] args) {
        ArgsParser parser = new ArgsParser(args);
        switch(parser.getValue("lang")) {
            case "ja":          // ja: 日本語
                locale = new Locale("ja", "JP");
                break;
            case "easy_ja":     // easy_ja: やさしい日本語
                locale = new Locale("jam", "JM");
                System.out.println(locale);
                break;
            case "en":          // en: 英語
                locale = new Locale("en", "US");
                break;
            default:            // 無指定: 日本語
                locale = Locale.getDefault();
                break;
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        startup(stage);
    }

    private void startup(Stage stage) {
        try {
            // Window size
            Rectangle2D d = Screen.getPrimary().getVisualBounds();
            int width = (int)Math.min(1280, d.getWidth());
            int height = (int)Math.min(720, d.getHeight());

            // Property
            URL propURLs[] = {getClass().getResource("/fxml/locale/")};
            URLClassLoader urlLoader = new URLClassLoader(propURLs);

            // Scene
            ResourceBundle resource = ResourceBundle.getBundle("locale", locale, urlLoader);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainUI.fxml"), resource);
            loader.setController(new MainUIController());
            Scene scene = new Scene(loader.load(), width, height);

            // Stage
            stage.setTitle("Tsunami Simulator - v3.3");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
