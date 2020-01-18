package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class AddWaveUIController implements Initializable {

    // UI部品
    @FXML
    private Button okBtn;
    @FXML
    private TextField distVal, heightVal;

    // 入力された値
    private int distance, height;

    /**
     * 初期化
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okBtn.setOnAction(event -> { checkInpValue(); okBtn.getScene().getWindow().hide(); });
    }

    /**
     * 入力の状況を返す
     */
    public boolean okInpStatus() {
        return distance != 0.0 && height != 0.0;
    }

    /**
     * distance, heightのゲッター
     */
    public int getDistance() { return distance; }
    public int getHeight() { return height; }

    /**
     * 入力値を検証する
     */
    private void checkInpValue() {
        try {
            distance = Integer.parseInt(distVal.getText());
            height = Integer.parseInt(heightVal.getText());
        } catch (Exception e) {}
    }

}