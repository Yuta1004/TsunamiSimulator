import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
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
import javafx.util.Duration;

import java.util.function.Consumer;
import java.util.function.BiConsumer;

import lib.NegativeBGAreaChart;

public class Main extends Application {

    // 定数
    private final int WIDTH = 1280, HEIGHT = 720;
    private final double TICK = 0.5;
    private final String TITLE = "Tsunami Simulator v0.0.1";

    // 描画用
    private Timeline tl;
    private Group root;
    private GraphicsContext gra;
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
        addBtn.accept("Init", (event)->{ initSimulator(); draw(); });
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
        root.getChildren().add(areaChart);
    }

    /**
     * シミュレータ初期化
     */
    private void initSimulator() {
        simulator = new TsunamiSimulator("../data/DEPTH.data");
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
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Tsunami");
        for(int idx = 0; idx < data.x.length; ++ idx) {
            series.getData().add(new XYChart.Data<Number, Number>(data.x[idx]/1000, data.z[idx]));
        }
        areaChart.getData().clear();
        areaChart.getData().add(series);
    }

}
