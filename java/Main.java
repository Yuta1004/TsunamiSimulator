import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Insets;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.util.Duration;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

import lib.NegativeBGAreaChart;
import Tsunami.TsunamiSimulator;
import Tsunami.TsunamiSimulatorUnevenness;
import Tsunami.StepData;

public class Main extends Application {

    // 定数
    private final int WIDTH = 1280, HEIGHT = 720;
    private final double TICK = 0.5;
    private final String TITLE = "Tsunami Simulator Java";

    // 描画用
    private String dataFile;
    private Group root;
    private GraphicsContext gra;
    private Timeline tl = new Timeline();
    private NegativeBGAreaChart<Number, Number> areaChart;
    private TsunamiSimulator simulator;

    /**
     * javafx
     * JavaFXアプリケーション開始時に1度呼ばれる
     *
     * @param Stage stage
     */
    @Override
    public void start(Stage stage){
        stage = setupStage(stage);
        setupDataFile(stage);
        setupAreaChart();
        stage.show();
    }

    /**
     * Stageの初期設定を行う
     *
     * @param Stage stage
     * @return Stage 設定済みStage
     */
    private Stage setupStage(Stage stage) {
        // Root, Scene, Canvas
        root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gra = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        setupUI();

        // Stage
        stage.setScene(scene);
        stage.setTitle(TITLE);
        return stage;
    }

    /**
     * データファイルの設定を行う
     *
     * @param Stage stage
     */
    private void setupDataFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select DEPTH.data");
        chooser.getExtensionFilters().add(
                    new ExtensionFilter("DataFile", "*.data", "*.txt")
                );
        File file = chooser.showOpenDialog(stage);
        dataFile = file == null ? "" : file.getAbsolutePath();
    }

    /**
     * UI部品の初期設定を行う
     */
    private void setupUI() {
        // Layout
        VBox parVBox = new VBox();
        VBox vbox = new VBox(15);
        parVBox.setLayoutX(1000);
        parVBox.setLayoutY(200);
        parVBox.setMargin(vbox, new Insets(40, 40, 40, 40));
        parVBox.getChildren().add(vbox);
        root.getChildren().add(parVBox);

        // ボタン
        BiConsumer<String, EventHandler<MouseEvent>> addBtn = (name, lambda) -> {
            Button btn = new Button(name);
            btn.addEventHandler(MouseEvent.MOUSE_CLICKED, lambda);
            btn.setPrefWidth(200);
            btn.setPrefHeight(50);
            vbox.getChildren().add(btn);
        };
        addBtn.accept("Start", (event)->startSimulate());
        addBtn.accept("Stop", (event)->tl.stop());
        addBtn.accept("Step", (event)->draw());
        addBtn.accept("Init", (event)->{ tl.stop(); initSimulator(); draw(); });
    }

    /**
     * AreaChartの初期設定を行う
     */
    private void setupAreaChart() {
        // x, y軸
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Distance(km)");
        yAxis.setLabel("Height(m)");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-10);
        yAxis.setUpperBound(20);

        // グラフ設定
        areaChart = new NegativeBGAreaChart<>(xAxis, yAxis);
        areaChart.setMinWidth(1000);
        areaChart.setMinHeight(HEIGHT);
        areaChart.setCreateSymbols(false);
        areaChart.setAnimated(false);
        root.getChildren().add(areaChart);
    }

    /**
     * シミュレータ初期化
     */
    private void initSimulator() {
        simulator = new TsunamiSimulatorUnevenness();
        simulator.setDepth(dataFile);
        simulator.setClock(12, 0, 0);           // 時計を12:00:00に
        simulator.setSimulateTime(3, 0, 0);     // 3時間分シミュレートする
        simulator.setItrTimeStep(0, 1, 0);      // 1分間隔でデータを取得する
        simulator.setWaveHeight(115, -2);       // 115kmの場所に-2mの波
        simulator.setWaveHeight(215, 5);        // 215kmの場所に5mの波
    }

    /**
     * シミュレート開始
     */
    private void startSimulate() {
        if(tl.getStatus().equals(Animation.Status.RUNNING))
            return;
        tl = new Timeline(
                new KeyFrame(
                    Duration.seconds(TICK),
                    event -> {
                        if(simulator == null || !simulator.hasNext())
                            tl.stop();
                        else
                            draw();
                    }
                )
            );
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    /**
     * 描画を行う
     */
    private void draw() {
        if(!simulator.hasNext())
            return;
        StepData data = simulator.next();

        // 津波
        drawTsunami(data);

        // 背景
        gra.setFill(Color.WHITE);
        gra.fillRect(0, 0, WIDTH, HEIGHT);

        // 時計
        gra.setFill(Color.BLACK);
        gra.setFont(new Font(40));
        gra.setTextAlign(TextAlignment.CENTER);
        gra.fillText(data.getStrClock(), 1140, 100);
    }

    /**
     * 津波を描画する
     *
     * @param StepData シミュレートデータ
     */
    private void drawTsunami(StepData data) {
        // XYChart設定
        XYChart.Series<Number, Number> seriesZ = new XYChart.Series<>();
        XYChart.Series<Number, Number> seriesDepth = new XYChart.Series<>();
        seriesZ.setName("Tsunami");
        seriesDepth.setName("Seabed");

        // データセット
        for(int idx = 0; idx < data.x.length; ++ idx) {
            seriesZ.getData().add(new XYChart.Data<Number, Number>(data.x[idx]/1000, data.z[idx]));
            seriesDepth.getData().add(new XYChart.Data<Number, Number>(data.x[idx]/1000, -data.depth[idx]));
        }
        areaChart.getData().clear();
        areaChart.getData().add(seriesZ);
        areaChart.getData().add(seriesDepth);

        // 色設定
        BiConsumer<XYChart.Series<Number, Number>, String> setColor = (series, color) -> {
            Node fill = series.getNode().lookup(".chart-series-area-fill");
            Node line = series.getNode().lookup(".chart-series-area-line");
            fill.setStyle("-fx-fill: rgba("+color+", 0.3);");
            line.setStyle("-fx-stroke: rgba("+color+", 1.0);");
        };
        setColor.accept(seriesZ, "70, 130, 255");
        setColor.accept(seriesDepth, "200, 120, 0");
    }

}
