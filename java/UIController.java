import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.File;

import lib.NegativeBGAreaChart;
import Tsunami.TsunamiSimulator;
import Tsunami.TsunamiSimulatorEvenness;
import Tsunami.TsunamiSimulatorUnevenness;

public class UIController implements Initializable {

    // シミュレータ
    private int simulatorMode;
    private TsunamiSimulator simulator;
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
    private Button upClockH, upClockM, downClockH, downClockM;

    /**
     * 初期化処理
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初期化
        initAreaChart();
        initSimulator((simulatorMode = EVENNESS));

        // UI部品にonActionを載せる
        setEvenness.setOnAction(event -> initSimulator(EVENNESS));
        setUnevenness.setOnAction(event -> initSimulator(UNEVENNESS));
        upClockH.setOnAction(event -> incClock(1, 0, 0));
        upClockM.setOnAction(event -> incClock(0, 1, 0));
        downClockH.setOnAction(event -> incClock(-1, 0, 0));
        downClockM.setOnAction(event -> incClock(0, -1, 0));
    }

    /**
     * simulatorを初期化する
     */
    private void initSimulator(int type) {
        // 初期化
        if(type == EVENNESS) {
            modeLabel.setText("Evenness");
            simulator = new TsunamiSimulatorEvenness();
        }
        if(type == UNEVENNESS) {
            modeLabel.setText("Unevenness");
            simulator = new TsunamiSimulatorUnevenness();
            simulator.setDepth(getFilePath());
        }

        // 設定
        simulator.setItrTimeStep(0, 1, 0);      // データ取得間間隔 => 1分
        simulator.setSimulateTime(6, 0, 0);     // シミュレート時間 => 6時間
    }

    /**
     * AreaChartを初期化する(NegativeBGAreaChart)
     */
    private void initAreaChart() {
        // x, y軸
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Distance(km)");
        yAxis.setLabel("Height(m)");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-10);
        yAxis.setUpperBound(20);

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
}