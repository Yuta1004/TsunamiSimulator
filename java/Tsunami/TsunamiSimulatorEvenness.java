package Tsunami;

public class TsunamiSimulatorEvenness extends TsunamiSimulator {

    private double depthVal, widthVal;

    /**
     * TsunamiSimulatorEvennessのコンストラクタ
     */
    public TsunamiSimulatorEvenness() {
        dataSize = 500;
    }

    /**
     * 波の高さをセットする
     *
     * @param pos 距離(km)
     * @param height 波の高さ(m)
     */
    @Override
    public void setWaveHeight(int pos, int height) {
        double posD = (double)widthVal/(pos*1000);
        for(int idx = 0; idx < dataSize; ++ idx)
            zp[idx] += height * Math.exp( -Math.pow(idx-dataSize/posD, 2) / Math.pow(dataSize/30.0, 2) );
    }

    /**
     * 海の深さ, モデル海洋の幅をセットする
     *
     * @param depth 深さ(m) Number
     * @param width モデル海洋の幅(km) Number
     */
    @Override
    public void setDepth(Object ... args) throws IllegalArgumentException {
        // 引数チェック
        if(args.length == 2 && args[0] instanceof Number && args[1] instanceof Number) {
            depthVal = ((Number)(args[0])).doubleValue();
            widthVal = ((Number)(args[1])).doubleValue()*1000;
        } else {
            throw new IllegalArgumentException();
        }

        // 計算用変数初期化
        x = new double[dataSize];
        depth = new double[dataSize];
        ub = new double[dataSize];
        up = new double[dataSize];
        uf = new double[dataSize];
        zb = new double[dataSize];
        zp = new double[dataSize];
        zf = new double[dataSize];
        dx = widthVal/dataSize;
        dt = 1;
        status = TsunamiSimulator.READY;

        // x, depthセット
        for(int idx = 0; idx < dataSize; ++ idx) {
            x[idx] = widthVal/dataSize*idx;
            depth[idx] = depthVal;
        }

    }

    @Override
    protected void step() {
        // 1. 未来ステップ値計算
        // 1.1. 水平流速更新
        for(int idx = 0; idx < dataSize-1; ++ idx) {
            if(step == 0)
                uf[idx] = up[idx] - grav*(dt/dx)*(zp[idx+1]-zp[idx]);
            else
                uf[idx] = ub[idx] - 2*grav*(dt/dx)*(zp[idx+1]-zp[idx]);
        }
        // 1.2. 海面変位更新
        for(int idx = 1; idx < dataSize-1; ++ idx) {
            if(step == 0)
                zf[idx] = zp[idx] - depthVal*(dt/dx)*(up[idx]-up[idx-1]);
            else
                zf[idx] = zb[idx] - 2*depthVal*(dt/dx)*(up[idx]-up[idx-1]);
        }

        // 2. 境界条件設定
        uf[0] = 0;
        uf[dataSize-1] = 0;

        // 3. 計算安定化処理(Asselin Filter)
        if(step > 0)
            for(int idx = 0; idx < dataSize; ++ idx) {
                if(idx < dataSize-1)
                    up[idx] += eps*(uf[idx]-2*up[idx]+ub[idx]);
                zp[idx] += eps*(zf[idx]-2*zp[idx]+zb[idx]);
            }

        // 4. ステップを進める
        for(int idx = 0; idx < dataSize; ++ idx) {
            ub[idx] = up[idx];
            up[idx] = uf[idx];
            zb[idx] = zp[idx];
            zp[idx] = zf[idx];
        }
    }

}