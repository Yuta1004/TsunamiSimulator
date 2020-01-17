import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

import java.net.URL;
import java.util.ResourceBundle;

import Tsunami.TsunamiSimulator;
import Tsunami.TsunamiSimulatorEvenness;
import Tsunami.TsunamiSimulatorUnevenness;

public class UIController implements Initializable {

    // シミュレータ
    private int simulatorMode;
    private TsunamiSimulator simulator;
    private static final int EVENNESS = 0;
    private static final int UNEVENNESS = 1;

    // UI部品
    @FXML
    private Label clockLabel, modeLabel;
    @FXML
    private MenuItem setEvenness, setUnevenness;

    /**
     * 初期化処理
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初期化
        initSimulator((simulatorMode = EVENNESS));

        // UI部品にonActionを載せる
        setEvenness.setOnAction(event -> initSimulator(EVENNESS));
        setUnevenness.setOnAction(event -> initSimulator(UNEVENNESS));
    }

    /**
     * simulatorを初期化する
     */
    private void initSimulator(int type) {
        // 初期化
        if(type == EVENNESS) {
            simulator = new TsunamiSimulatorEvenness();
            modeLabel.setText("Evenness");
        }
        if(type == UNEVENNESS) {
            simulator = new TsunamiSimulatorUnevenness();
            modeLabel.setText("Unevenness");
        }

        // 設定
        simulator.setItrTimeStep(0, 1, 0);      // データ取得間間隔 => 1分
        simulator.setSimulateTime(6, 0, 0);     // シミュレート時間 => 6時間
    }

}