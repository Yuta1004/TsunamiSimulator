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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.StageStyle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.BiConsumer;

import controller.AddWaveUIController;
import controller.MakeMapUIController;
import lib.NegativeBGAreaChart;
import tsunami.StepData;
import tsunami.TsunamiSimulator;
import tsunami.TsunamiSimulatorConstant;
import tsunami.TsunamiSimulatorVariable;

public class MainUIController implements Initializable {

    // アニメーション
    private Timeline tl = new Timeline();
    private double TICK = 0.5;

    // 定数
    private static final int CONSTANT = 0;
    private static final int VARIABLE= 1;
    private static final int PRESET_SENDAI = 1;
    private static final int PRESET_TOSA = 2;
    private static final int PRESET_TOKAI = 3;
    private static final int PRESET_HOKKAIDO = 4;
    private URL presets[];

    // リソース
    ResourceBundle resource;

    // シミュレータ
    private int simulatorMode;
    private TsunamiSimulator simulator;
    private NegativeBGAreaChart<Number, Number> tsunamiChart;

    // UI部品
    @FXML
    private Label clockLabel, leftStatusLabel;
    @FXML
    private MenuItem setVariableFromFile, setVariableSendai, setVariableTosa, setVariableTokai,
            setVariableHokkaido, setConstant, addWaveMenu, openMakeMap, openCredit;
    @FXML
    private AnchorPane chartPane;
    @FXML
    private Button upClockH, upClockM, downClockH, downClockM, initBtn, initBtn2, startBtn, stopBtn, stepBtn, resetBtn;
    @FXML
    private TextField depthVal, upperHeightVal, lowerHeightVal, upperWidthVal, lowerWidthVal;

    /**
     * 初期化処理
     */
    @Override
    public void initialize(URL location, ResourceBundle resource) {
        // スプラッシュ表示
        Stage splash = genStage("", "/fxml/Credit.fxml", new SplashController());
        splash.getScene().setFill(Color.TRANSPARENT);
        splash.initStyle(StageStyle.TRANSPARENT);
        splash.showAndWait();

        // プリセットデータ
        presets = new URL[6];
        presets[PRESET_SENDAI+1] = getClass().getResource("/data/SENDAI.data");
        presets[PRESET_TOSA+1] = getClass().getResource("/data/TOSA.data");
        presets[PRESET_TOKAI+1] = getClass().getResource("/data/TOKAI.data");
        presets[PRESET_HOKKAIDO+1] = getClass().getResource("/data/HOKKAIDO.data");

        // 初期化
        this.resource = resource;
        initSimulator();
        initAreaChart();

        // UI部品にactionを載せる
        // 1. Button;
        stopBtn.setOnAction(event -> tl.stop());
        initBtn.setOnAction(event -> initSimulator());
        initBtn2.setOnAction(event-> initSimulator());
        startBtn.setOnAction(event -> initTimeline());
        stepBtn.setOnAction(event -> { simulator.next(); draw(); });
        resetBtn.setOnAction(event -> { tl.stop(); simulator.reset(); draw(); });
        upClockH.setOnAction(event -> { simulator.incClock(1, 0, 0); updateClock(); });
        upClockM.setOnAction(event -> { simulator.incClock(0, 1, 0); updateClock(); });
        downClockH.setOnAction(event -> { simulator.incClock(-1, 0, 0); updateClock(); });
        downClockM.setOnAction(event -> { simulator.incClock(0, -1, 0); updateClock(); });
        // 2. MenuItem
        addWaveMenu.setOnAction(event -> addWave());
        setConstant.setOnAction(event -> changeMode(CONSTANT));
        setVariableFromFile.setOnAction(event -> changeMode(VARIABLE));
        setVariableTosa.setOnAction(event -> changeMode(VARIABLE+PRESET_TOSA));
        setVariableTokai.setOnAction(event -> changeMode(VARIABLE+PRESET_TOKAI));
        setVariableSendai.setOnAction(event -> changeMode(VARIABLE+PRESET_SENDAI));
        setVariableHokkaido.setOnAction(event -> changeMode(VARIABLE+PRESET_HOKKAIDO));
        openCredit.setOnAction(event -> genStage("Credit", "/fxml/Credit.fxml", null).showAndWait());
        openMakeMap.setOnAction(event-> genStage("MakeMap" ,"/fxml/MakeMap.fxml", new MakeMapUIController()).showAndWait());
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
        initSimulator();
    }

    /**
     * simulatorを初期化する
     */
    private void initSimulator() {
        tl.stop();

        if(simulatorMode == CONSTANT) {
            double depth;
            try {
                depth = Double.parseDouble(depthVal.getText());
            } catch(Exception e){ return; }
            simulator = new TsunamiSimulatorConstant();
            simulator.setDepth(depth, 500);
            lowerWidthVal.setText("0");
        }

        if(simulatorMode >= VARIABLE) {
            lowerWidthVal.setText("-12");
            URL dataURL = simulatorMode == VARIABLE ? getFilePath() : presets[simulatorMode];
            TsunamiSimulator oldSimulator = simulator;
            simulator = new TsunamiSimulatorVariable();
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
        tsunamiChart.setVerticalZeroLineVisible(false);
        tsunamiChart.setHorizontalZeroLineVisible(false);

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
        // ダイアログ立ち上げ
        AddWaveUIController controller = new AddWaveUIController();
        genStage("AddWave", "/fxml/AddWaveUI.fxml", controller).showAndWait();

        // 波追加
        if(controller.okInpStatus()) {
            int distance = controller.getDistance();
            int height = controller.getHeight();
            if(Math.abs(height) > 10.0) {
                setStatusMsg("波の高さは-10m以上10m以下である必要があります");
                return;
            }
            simulator.setWaveHeight(distance, height);
            draw();
        }
    }

    /**
     * 指定FXMlを元にStageを生成して返す
     *
     * @param title タイトル
     * @param fxmlPath FXMLファイルのパス
     * @param controller UIコントローラ
     * @return Stage
     */
    private <T> Stage genStage(String title, String fxmlPath, T controller) {
        // FXML読み込み
        Scene scene = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), resource);
            if(controller != null)
                loader.setController(controller);
            scene = new Scene(loader.load());
        } catch (Exception e){ e.printStackTrace(); return null; }

        // ダイアログ立ち上げ
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle("TsunamiSimulator - "+title);
        return stage;
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

    /**
     * ウィンドウに表示するステータスを更新する
     *
     * @param msg メッセージ
     */
    private void setStatusMsg(String msg) {
        leftStatusLabel.setText(msg);
    }

}