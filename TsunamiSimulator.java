import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Iterator;

public class TsunamiSimulator implements Iterable<StepData>, Iterator<StepData>{

    // 定数
    static final int H = 60*60;
    static final int M = 60;
    static final int S = 1;
    static final double grav = 9.8;

    // 時間データ
    int clock = 0*H + 0*M + 0*S;
    int timeEnd = 3*H, timeStep = 1*M;

    // 計算用変数
    int dataSize = -1;
    double dx, dt;
    double ub[], up[], uf[];    // 水平流速
    double zb[], zp[], zf[];    // 海面変位
    double x[], depth[];        // 位置(m)、深さ(m)

    /**
     * TsunamiSimulatorのコンストラクタ
     * データ読み込み, 初期値の設定を行う
     *
     * @param depthFilePath 地形データファイルのパス
     */
    public TsunamiSimulator(String depthFilePath) {
        loadDepthData(depthFilePath);
        ub = new double[dataSize];
        up = new double[dataSize];
        uf = new double[dataSize];
        zb = new double[dataSize];
        zp = new double[dataSize];
        zf = new double[dataSize];
        dx = (x[dataSize-1]-x[0]) / dataSize;
        dt = 0.5;
    }

    /**
     * 指定位置の波の高さをセットする
     *
     * @param pos 距離(m)
     * @param height 高さ(m)
     */
    public void setWaveHeight(int pos, int height) {
        for(int idx = 0; idx < dataSize; ++ idx) {
            zp[idx] += height * Math.exp( Math.pow(-(x[idx]-pos)*1000, 2) / Math.pow(40*1000, 2) );
        }
    }

    /**
     * シミュレート開始時刻をセットする
     *
     * @param hour 時
     * @param min 分
     * @param sec 秒
     */
    public void setClock(int hour, int min, int sec) {
        clock = hour*H + min*M + sec*S;
    }

    /**
     * シミュレート時間をセットする
     *
     * @param hour 時
     * @param min 分
     * @param sec 秒
     */
    public void setSimulateTime(int hour, int min, int sec) {
        timeEnd = hour*H + min*M + sec*S;
    }

    /**
     * シミュレートする際の時間ステップをセットする
     *
     * @param hour 時
     * @param min 分
     * @param sec 秒
     */
    public void setTimeStep(int hour, int min, int sec) {
        timeStep = hour*H + min*M + sec*S;
    }

    /**
     * Iterator
     *
     * @return Iterator<StepData>
     */
    @Override
    public Iterator<StepData> iterator() {
        return this;
    }

    /**
     * Itarable
     *
     * @return boolean まだステップが続くか
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    /**
     * Itarable
     *
     * @return StepData ステップのデータ
     */
    @Override
    public StepData next() {
         return new StepData(0, 0, null, null);
    }

    /**
     * 地形データを読み込んでx, depthにセットする
     *
     * @param depthFilePath 地形データファイルのパス
     */
    private void loadDepthData(String depthFilePath) {
        // 存在チェック
        Path path = Paths.get(depthFilePath);
        if(!(path.toFile().exists()))
            error("地形データファイルが存在しません => "+path);

        // データファイル読み込み
        List<String> dataLines = null;
        try {
            dataLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch(Exception e) {
            e.printStackTrace();
            error("地形データファイル読み込み中にエラーが発生しました");
        }
        dataSize = dataLines.size();

        // 値設定
        x = new double[dataSize];
        depth = new double[dataSize];
        for(int idx = 0; idx < dataLines.size(); ++ idx) {
            String line = dataLines.get(idx);                       // x<\t>depth<\n>
            x[idx] = Double.parseDouble(line.split("\t")[0]);       // x        (str->double)
            x[idx] *= 1000;                                         // km->m
            depth[idx] = Double.parseDouble(line.split("\t")[1]);   // depth    (str->double)
        }
    }

    /**
     * エラーを吐く
     * 標準エラー出力にエラーであることを示すメッセージを出力する
     *
     * @param msg メッセージ
     */
    private void error(String msg) {
        System.err.println("[ERROR] "+ msg);
        System.exit(0);
    }

}
