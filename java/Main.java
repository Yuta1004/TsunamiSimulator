import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;

public class Main extends Application {

    // 定数
    private final int WIDTH = 1280, HEIGHT = 720;
    private final String TITLE = "Tsunami Simulator v0.0.1";

    // 描画用
    private Group root;
    private AreaChart<Number, Number> areaChart;
    private TsunamiSimulator simulator;

    /**
     * Mainクラスのコンストラクタ
     */
    public Main() {
        // シミュレータ設定
        simulator = new TsunamiSimulator("../data/DEPTH.data");
        simulator.setClock(12, 0, 0);           // 時計を12:00:00に
        simulator.setSimulateTime(3, 0, 0);     // 3時間分シミュレートする
        simulator.setItrTimeStep(0, 1, 0);      // 1分間隔でデータを取得する
        simulator.setWaveHeight(115, -2);       // 115kmの場所に-2mの波
        simulator.setWaveHeight(215, 5);        // 215kmの場所に5mの波
    }

    /**
     * javafx
     * JavaFXアプリケーション開始時に1度呼ばれる
     *
     * @param Stage stage
     */
    @Override
    public void start(Stage stage){
        stage = setupStage(stage);
        setupAreaChart();
        stage.show();

        drawTsunami();
    }

    /**
     * Stageの初期設定を行う
     *
     * @param Stage stage
     * @return Stage 設定済みStage
     */
    private Stage setupStage(Stage stage) {
        root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setTitle(TITLE);
        return stage;
    }

    /**
     * AreaChartの初期設定を行う
     */
    private void setupAreaChart() {
        // x, y軸
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Disttance(km)");
        yAxis.setLabel("Height(m)");

        // グラフ設定
        areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setMinWidth(1280);
        areaChart.setMinHeight(720);
        areaChart.setCreateSymbols(false);
        root.getChildren().add(areaChart);
    }

    /**
     * 津波を描画する
     */
    private void drawTsunami() {
        StepData data = simulator.next();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for(int idx = 0; idx < data.x.length; ++ idx) {
            series.getData().add(new XYChart.Data<Number, Number>(data.x[idx]/1000, data.z[idx]));
        }
        areaChart.getData().add(series);
    }

}
