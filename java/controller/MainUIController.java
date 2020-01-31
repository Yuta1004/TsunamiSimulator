package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import controller.AddWaveUIController;
import lib.NegativeBGAreaChart;
import tsunami.StepData;
import tsunami.TsunamiSimulator;
import tsunami.TsunamiSimulatorEvenness;
import tsunami.TsunamiSimulatorUnevenness;

public class MainUIController implements Initializable {

    // アニメーション
    private Timeline tl = new Timeline();
    private double TICK = 0.5;

    // 定数
    private static final int EVENNESS = 0;
    private static final int UNEVENNESS = 1;
    private static final int PRESET_SENDAI = 1;
    private URL presets[];

    // シミュレータ
    private int simulatorMode;
    private TsunamiSimulator simulator;
    private NegativeBGAreaChart<Number, Number> tsunamiChart;

    // UI部品
    @FXML
    private Label clockLabel, modeLabel;
    @FXML
    private MenuItem setUnevennessFromFile, setUnevennessSendai, setEvenness, addWaveMenu;
    @FXML
    private AnchorPane chartPane;
    @FXML
    private Button upClockH, upClockM, downClockH, downClockM, initBtn, startBtn, stopBtn, stepBtn, resetBtn;
    @FXML
    private TextField depthVal, upperHeightVal, lowerHeightVal, upperWidthVal, lowerWidthVal;
    @FXML
    private HBox depthHBox;

    /**
     * 初期化処理
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // プリセットデータ
        presets = new URL[3];
        presets[PRESET_SENDAI+1] = getClass().getResource("/data/SENDAI.data");

        // 初期化
        initSimulator();
        initAreaChart();

        // UI部品にactionを載せる
        // 1. Button;
        initBtn.setOnAction(event -> initSimulator());
        resetBtn.setOnAction(event -> { tl.stop(); simulator.reset(); draw(); });
        startBtn.setOnAction(event -> initTimeline());
        stepBtn.setOnAction(event -> { simulator.next(); draw(); });
        stopBtn.setOnAction(event -> tl.stop());
        upClockH.setOnAction(event -> { simulator.incClock(1, 0, 0); updateClock(); });
        upClockM.setOnAction(event -> { simulator.incClock(0, 1, 0); updateClock(); });
        downClockH.setOnAction(event -> { simulator.incClock(-1, 0, 0); updateClock(); });
        downClockM.setOnAction(event -> { simulator.incClock(0, -1, 0); updateClock(); });
        // 2. MenuItem
        setEvenness.setOnAction(event -> changeMode(EVENNESS));
        setUnevennessFromFile.setOnAction(event -> changeMode(UNEVENNESS));
        setUnevennessSendai.setOnAction(event -> changeMode(UNEVENNESS+PRESET_SENDAI));
        addWaveMenu.setOnAction(event -> addWave());
        // 3. TextArea
        upperWidthVal.textProperty().addListener((obs, oldText, newText) -> initAreaChart());
        lowerWidthVal.textProperty().addListener((obs, oldText, newText) -> initAreaChart());
        upperHeightVal.textProperty().addListener((obs, oldText, newText) -> initAreaChart());
        lowerHeightVal.textProperty().addListener((obs, oldText, newText) -> initAreaChart());
    }

    /**
     * モード変更
     */
    private void changeMode(int mode) {
        simulatorMode = mode;
        if(mode == EVENNESS)
            modeLabel.setText("Evenness");
        else
            modeLabel.setText("Unevenness");
        depthHBox.setVisible(mode == EVENNESS);
        initSimulator();
    }

    /**
     * simulatorを初期化する
     */
    private void initSimulator() {
        tl.stop();

        if(simulatorMode == EVENNESS) {
            double depth;
            try {
                depth = Double.parseDouble(depthVal.getText());
            } catch(Exception e){ return; }
            simulator = new TsunamiSimulatorEvenness();
            simulator.setDepth(depth, 500);
        }

        if(simulatorMode >= UNEVENNESS) {
            URL dataURL = simulatorMode == UNEVENNESS ? getFilePath() : presets[simulatorMode];
            TsunamiSimulator oldSimulator = simulator;
            simulator = new TsunamiSimulatorUnevenness();
            if(!simulator.setDepth(dataURL))
                simulator = oldSimulator;
        }

        // 設定
        simulator.setItrTimeStep(0, 1, 0);      // データ取得間間隔 => 1分
        draw();
    }

    /**
     * AreaChartを初期化する(NegativeBGAreaChart)
     */
    private void initAreaChart() {
        // 値取得
        double upperHeight, lowerHeight, upperWidth, lowerWidth;
        try {
            upperHeight = Double.parseDouble(upperHeightVal.getText());
            lowerHeight = Double.parseDouble(lowerHeightVal.getText());
            upperWidth = Double.parseDouble(upperWidthVal.getText());
            lowerWidth = Double.parseDouble(lowerWidthVal.getText());
        } catch (Exception e) { return; }

        // x, y軸
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Distance(km)");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(lowerWidth);
        xAxis.setUpperBound(upperWidth);
        yAxis.setLabel("Height(m)");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(lowerHeight);
        yAxis.setUpperBound(upperHeight);

        // グラフ設定
        tsunamiChart = new NegativeBGAreaChart<>(xAxis, yAxis);
        tsunamiChart.setCreateSymbols(false);
        tsunamiChart.setAnimated(false);

        // AnchorPane設定
        AnchorPane.setTopAnchor(tsunamiChart, 10.0);
        AnchorPane.setLeftAnchor(tsunamiChart, 10.0);
        AnchorPane.setRightAnchor(tsunamiChart, 10.0);
        AnchorPane.setBottomAnchor(tsunamiChart, 10.0);
        chartPane.getChildren().clear();
        chartPane.getChildren().add(tsunamiChart);
        draw();
    }

    /**
     * Timeline初期化
     */
    private void initTimeline() {
        if(tl.getStatus().equals(Animation.Status.RUNNING))
            return;
        tl = new Timeline(
                new KeyFrame(
                    Duration.seconds(TICK),
                    event -> { simulator.next(); draw(); }
                )
            );
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    /**
     * 時刻表示更新
     */
    private void updateClock() {
        int H = 60*60, M = 60, S = 1;
        int clock = simulator.getClock();
        clockLabel.setText(String.format("%02d:%02d:%02d", clock/H, clock/M%M, clock%M));
    }

    /**
     * ファイル選択を行ってもらい、その結果を返す
     */
    private URL getFilePath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select DEPTH.data");
        chooser.getExtensionFilters().add(
                    new ExtensionFilter("DataFile", "*.data", "*.txt")
                );
        File file = chooser.showOpenDialog((Stage)chartPane.getScene().getWindow());
        try {
            return file == null ? new URL("file:///null") : file.toURI().toURL();
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * 新しいダイアログを立ち上げて入力された内容をもとに波を追加する
     */
    private void addWave() {
        // FXML読み込み
        AddWaveUIController controller = new AddWaveUIController();
        Scene scene = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddWaveUI.fxml"));
            loader.setController(controller);
            scene = new Scene(loader.load());
        } catch (Exception e){ e.printStackTrace(); return; }

        // ダイアログ立ち上げ
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();

        // 波追加
        if(controller.okInpStatus()) {
            int distance = controller.getDistance();
            int height = controller.getHeight();
            simulator.setWaveHeight(distance, height);
            draw();
        }
    }

    /**
     * 津波を描画する
     */
    private void draw() {
        if(tsunamiChart == null) return;
        StepData tsunamiData = simulator.getData();

        // データセット
        XYChart.Series<Number, Number> seriesZ = new XYChart.Series<>();
        XYChart.Series<Number, Number> seriesDepth = new XYChart.Series<>();
        for(int idx = 0; idx < tsunamiData.x.length; ++ idx) {
            seriesZ.getData().add(
                new XYChart.Data<Number, Number>(tsunamiData.x[idx]/1000, tsunamiData.z[idx])
            );
            seriesDepth.getData().add(
                new XYChart.Data<Number, Number>(tsunamiData.x[idx]/1000, -tsunamiData.depth[idx])
            );
        }
        tsunamiChart.getData().clear();
        tsunamiChart.getData().add(seriesZ);
        tsunamiChart.getData().add(seriesDepth);
        tsunamiChart.setLegendVisible(false);
        updateClock();

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