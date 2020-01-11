import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.stage.Stage;

public class Main extends Application {

    // 定数
    private final int WIDTH = 1280, HEIGHT = 720;
    private final String TITLE = "Tsunami Simulator v0.0.1";

    // その他
    private TsunamiSimulator simulator;

    public Main() {
        // シミュレータ設定
        simulator = new TsunamiSimulator("../data/DEPTH.data");
        simulator.setClock(12, 0, 0);           // 時計を12:00:00に
        simulator.setSimulateTime(3, 0, 0);     // 3時間分シミュレートする
        simulator.setItrTimeStep(0, 1, 0);      // 1分間隔でデータを取得する
        simulator.setWaveHeight(115, -2);       // 115mの場所に-2mの波
        simulator.setWaveHeight(215, 5);        // 215mの場所に5mの波
    }

    @Override
    public void start(Stage stage){
        stage = setupStage(stage);
        stage.show();
    }

    private Stage setupStage(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setTitle(TITLE);
        return stage;
    }


}
