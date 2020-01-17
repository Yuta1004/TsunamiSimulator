import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.File;
import java.util.function.BiConsumer;

import lib.NegativeBGAreaChart;
import Tsunami.StepData;
import Tsunami.TsunamiSimulator;
import Tsunami.TsunamiSimulatorEvenness;
import Tsunami.TsunamiSimulatorUnevenness;

public class UIController implements Initializable {

    // アニメーション
    private Timeline tl = new Timeline();
    private double TICK = 0.5;

    // シミュレータ
    private int simulatorMode;
    private TsunamiSimulator simulator;
    private StepData tsunamiData;
    private static final int EVENNESS = 0;
    private static final int UNEVENNESS = 1;
    private NegativeBGAreaChart<Number, Number> tsunamiChart;

    // UI部品
    @FXML
    private Label clockLabel, modeLabel;
    @FXML
    private MenuItem setEvenness, setUnevenness;
    @FXML
    private AnchorPane chartPane;
    @FXML
    private Button upClockH, upClockM, downClockH, downClockM, initBtn, startBtn, stopBtn, stepBtn;
    @FXML
    private TextField widthVal, depthVal, upperHeightVal, lowerHeightVal;
    @FXML
    private HBox widthHBox, depthHBox;

    /**
     * 初期化処理
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初期化
        initAreaChart();

        // UI部品にactionを載せる
        initBtn.setOnAction(event -> initSimulator());
        startBtn.setOnAction(event -> initTimeline());
        stepBtn.setOnAction(event -> { if(simulator.hasNext()) tsunamiData = simulator.next(); drawTsunami(); });
        stopBtn.setOnAction(event -> tl.stop());
        setEvenness.setOnAction(event -> changeMode(EVENNESS)) ;
        setUnevenness.setOnAction(event -> changeMode(UNEVENNESS));
        upClockH.setOnAction(event -> incClock(1, 0, 0));
        upClockM.setOnAction(event -> incClock(0, 1, 0));
        downClockH.setOnAction(event -> incClock(-1, 0, 0));
        downClockM.setOnAction(event -> incClock(0, -1, 0));
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
        widthHBox.setVisible(mode == EVENNESS);
        depthHBox.setVisible(mode == EVENNESS);
    }

    /**
     * simulatorを初期化する
     */
    private void initSimulator() {
        // 初期化
        tl.stop();
        if(simulatorMode == EVENNESS) {
            double depth, width;
            try {
                depth = Double.parseDouble(depthVal.getText());
                width = Double.parseDouble(widthVal.getText());
            } catch(Exception e){ return; }
            simulator = new TsunamiSimulatorEvenness();
            simulator.setDepth(depth, width);
        }
        if(simulatorMode == UNEVENNESS) {
            simulator = new TsunamiSimulatorUnevenness();
            simulator.setDepth(getFilePath());
        }

        // 設定
        simulator.setItrTimeStep(0, 1, 0);      // データ取得間間隔 => 1分
        simulator.setSimulateTime(6, 0, 0);     // シミュレート時間 => 6時間
        simulator.setWaveHeight(115, -2);
        simulator.setWaveHeight(225, 5);
        tsunamiData = simulator.next();
        drawTsunami();
    }

    /**
     * AreaChartを初期化する(NegativeBGAreaChart)
     */
    private void initAreaChart() {
        // 値取得
        double upperHeight, lowerHeight;
        try {
            upperHeight = Double.parseDouble(upperHeightVal.getText());
            lowerHeight = Double.parseDouble(lowerHeightVal.getText());
        } catch (Exception e) { return; }

        // x, y軸
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Distance(km)");
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
        drawTsunami();
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
                    event -> {
                        if(simulator == null || !simulator.hasNext()) {
                            tl.stop();
                        } else {
                            tsunamiData =simulator.next();
                            drawTsunami();
                         }
                    }
                )
            );
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    /**
     * 時刻設定
     */
    private void incClock(int hour, int min, int sec) {
        int H = 60*60, M = 60, S = 1;
        int clock = simulator.getClock();
        clock += hour*H + min*M + sec*S;
        simulator.setClock(clock/H, clock/M%M, clock%M);
        clock = simulator.getClock();
        clockLabel.setText(String.format("%02d:%02d:%02d", clock/H, clock/M%M, clock%M));
    }

    /**
     * ファイル選択を行ってもらい、その結果を返す
     */
    private String getFilePath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select DEPTH.data");
        chooser.getExtensionFilters().add(
                    new ExtensionFilter("DataFile", "*.data", "*.txt")
                );
        File file = chooser.showOpenDialog((Stage)chartPane.getScene().getWindow());
        return file == null ? "" : file.getAbsolutePath();
    }

    /**
     * 津波を描画する
     */
    private void drawTsunami() {
        if(tsunamiData == null)
            return;

        // XYChart設定
        XYChart.Series<Number, Number> seriesZ = new XYChart.Series<>();
        XYChart.Series<Number, Number> seriesDepth = new XYChart.Series<>();
        seriesZ.setName("Tsunami");
        seriesDepth.setName("Seabed");

        // データセット
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