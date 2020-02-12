package tsunami;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.net.URL;

public class TsunamiSimulatorVariable extends TsunamiSimulator {

    /**
     * 指定位置の波の高さをセットする
     *
     * @param pos 距離(km)
     * @param height 高さ(m)
     */
    @Override
    public void setWaveHeight(int pos, int height) {
        for(int idx = 0; idx < dataSize; ++ idx) {
            zp[idx] += height * Math.exp( -Math.pow(x[idx]-pos*1000, 2) / Math.pow(40*1000, 2) );
        }
    }

    /**
     * 地形データを読み込んでx, depthにセットする
     *
     * @param path 地形データのパス
     * @throws IllegalArgumentException 引数の方が想定と異なる場合投げる
     */
    @Override
    public boolean setDepth(Object ... args) throws IllegalArgumentException {
        // 引数チェック
        URL depthFileURL;
        if(args[0] instanceof URL)
            depthFileURL = (URL)args[0];
       else
           throw new IllegalArgumentException();

        // データファイル読み込み
        ArrayList<String> dataLines = new ArrayList<String>();
        try {
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(depthFileURL.openStream()));
            while((line = br.readLine()) != null)
                dataLines.add(line);
        } catch(FileNotFoundException e) {
            error("指定されたファイルは存在しません");
            return false;
        } catch(IOException e) {
            error("データファイル読み込み中にエラーが発生しました");
            return false;
        }
        dataSize = dataLines.size();

        // x, depth設定
        x = new double[dataSize];
        depth = new double[dataSize];
        for(int idx = 0; idx < dataLines.size(); ++ idx) {
            String line = dataLines.get(idx);                       // x<\t>depth<\n>
            x[idx] = Double.parseDouble(line.split("\t")[0]);       // x        (str->double)
            x[idx] *= 1000;                                         // km->m
            depth[idx] = Double.parseDouble(line.split("\t")[1]);   // depth    (str->double)
            depth[idx] *= -1;                                       // +/-
        }

        // その他計算用変数初期化
        ub = new double[dataSize];
        up = new double[dataSize];
        uf = new double[dataSize];
        zb = new double[dataSize];
        zp = new double[dataSize];
        zf = new double[dataSize];
        dx = (x[dataSize-1]-x[0]) / dataSize;
        dt = 0.5;
        return true;
    }

    /**
     * シミュレータを1ステップ進める
     */
    @Override
    protected void step() {
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
        for(int idx = 0; idx < dataSize; ++ idx) {
            ub[idx] = up[idx];
            up[idx] = uf[idx];
            zb[idx] = zp[idx];
            zp[idx] = zf[idx];
        }
    }

    /**
     * エラーを吐いてステータスをERRORにする
     */
    private void error(String msg) {
        System.out.println("[ERROR] "+msg);
    }

}
