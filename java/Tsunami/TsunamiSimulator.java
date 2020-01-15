package Tsunami;

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
    private static final int H = 60*60;
    private static final int M = 60;
    private static final int S = 1;
    public static final double grav = 9.8;
    public static final double eps = 0.01;

    // ステータス
    private int status = 0;
    public static final int READY = 0;
    public static final int RUNNING = 1;
    public static final int ERROR = 2;

    // 時間データ
    protected int clock = 0*H + 0*M + 0*S;
    protected int timeEnd = 3*H, itrTimeStep = 1*M;

    // 計算用変数
    private int step, dataSize;
    private double dx, dt;
    private double ub[], up[], uf[];    // 水平流速
    private double zb[], zp[], zf[];    // 海面変位
    private double x[], depth[];        // 位置(m)、深さ(m)

    /**
     * TsunamiSimulatorのコンストラクタ
     * データ読み込み, 初期値の設定を行う
     *
     * @param depthFilePath 地形データファイルのパス
     */
    public TsunamiSimulator(String depthFilePath) {
        loadDepthData(depthFilePath);
        if(status == ERROR)
            return;
        ub = new double[dataSize];
        up = new double[dataSize];
        uf = new double[dataSize];
        zb = new double[dataSize];
        zp = new double[dataSize];
        zf = new double[dataSize];
        dx = (x[dataSize-1]-x[0]) / dataSize;
        step = 0;
        dt = 0.5;
    }

    /**
     * 指定位置の波の高さをセットする
     *
     * @param pos 距離(km)
     * @param height 高さ(m)
     */
    public void setWaveHeight(int pos, int height) {
        for(int idx = 0; idx < dataSize; ++ idx) {
            zp[idx] += height * Math.exp( -Math.pow(x[idx]-pos*1000, 2) / Math.pow(40*1000, 2) );
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
     * データを取得する(1イテレータ進める)時間間隔をセットする
     *
     * @param hour 時
     * @param min 分
     * @param sec 秒
     */
    public void setItrTimeStep(int hour, int min, int sec) {
        itrTimeStep = hour*H + min*M + sec*S;
    }

    /**
     * 現在のステータスを返す
     *
     * @return status
     */
    public int getStatus() {
        return status;
    }


    /**
     * Iterator
     *
     * @return Iterator<StepData>
     */
    @Override
    public Iterator<StepData> iterator() {
        status = RUNNING;
        return this;
    }

    /**
     * Itarable
     *
     * @return boolean まだイテレータが続くか
     */
    @Override
    public boolean hasNext() {
        return step < ((double)(timeEnd+1)/dt) && status != ERROR;
    }

    /**
     * Itarable
     * 1イテレート分ステップを進める
     *
     * @return StepData シミュレートデータ
     */
    @Override
    public StepData next() {
        if(status == ERROR)
            return null;
        StepData sdata = new StepData(clock, step, x, zp, depth);
        for(int idx = 0; idx < (int)((double)itrTimeStep/dt); ++ idx)
            step();
        clock += itrTimeStep;
        return sdata;
    }

    /**
     * 地形データを読み込んでx, depthにセットする
     *
     * @param depthFilePath 地形データファイルのパス
     */
    private void loadDepthData(String depthFilePath) {
        // 存在チェック
        Path path = Paths.get(depthFilePath);
        if(!(path.toFile().exists())) {
            error("地形データファイルが存在しません => "+path);
            return;
        }

        // データファイル読み込み
        List<String> dataLines = null;
        try {
            dataLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch(Exception e) {
            e.printStackTrace();
            error("地形データファイル読み込み中にエラーが発生しました");
            return;
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
            depth[idx] *= -1;                                       // +/-
        }
        status = READY;
    }

    /**
     * シミュレータを1ステップ進める
     */
    private void step() {
        // 1. 未来ステップ値計算
        // 1-1. 水平流速更新
        for(int idx = 0; idx < dataSize-1; ++ idx) {
            if(step == 0)
                uf[idx] = up[idx] - grav*(dt/dx)*(zp[idx+1]-zp[idx]);
            else
                uf[idx] = ub[idx] - 2*grav*(dt/dx)*(zp[idx+1]-zp[idx]);
        }
        // 1-2. 海面変位更新
        for(int idx = 1; idx < dataSize-1; ++ idx) {
            double depth0 = (depth[idx]+depth[idx-1]) * 0.5;
            double depth1 = (depth[idx]+depth[idx+1]) * 0.5;
            if(step == 0)
                zf[idx] = zp[idx] - (dx/dx)*(depth1*up[idx]-depth0*up[idx-1]);
            else
                zf[idx] = zb[idx] - 2*(dt/dx)*(depth1*up[idx]-depth0*up[idx-1]);
        }

        // 2. 陸上で流速を0にする
        for(int idx = 0; idx < dataSize-1; ++ idx) {
            if((depth[idx]+depth[idx+1])*0.5 <= 0)
                uf[idx] = 0;
        }

        // 3. 沖側に伝わる津波を強制的に減衰させる
        for(int idx = dataSize-50; idx < dataSize; ++ idx) {
            zf[idx] *= (dataSize-idx)/50.0;
            uf[idx] *= (dataSize-idx)/50.0;
        }

        // 4. 境界条件セット
        zf[0] = 0;
        zf[dataSize-1] = 0;

        // 5. 計算安定化処理
        if(step > 0)
            for(int idx = 0; idx < dataSize; ++ idx) {
                if(idx < dataSize-1)
                    up[idx] += eps * (uf[idx]-2*up[idx]+ub[idx]);
                zp[idx] += eps * (zf[idx]-2*zp[idx]+zb[idx]);
            }

        // 6. ステップ更新
        ++ step;
        for(int idx = 0; idx < dataSize; ++ idx) {
            ub[idx] = up[idx];
            up[idx] = uf[idx];
            zb[idx] = zp[idx];
            zp[idx] = zf[idx];
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
        status = ERROR;
    }

}
