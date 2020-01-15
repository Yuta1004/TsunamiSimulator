package Tsunami;

public class TsunamiSimulatorEvenness extends TsunamiSimulator {

    private double depth, width;

    /**
     * TsunamiSimulatorEvennessのコンストラクタ
     */
    TsunamiSimulatorEvenness() {
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
        for(int idx = 0; idx < dataSize; ++ idx)
            zp[idx] = Math.exp( -(Math.pow(idx-dataSize/2, 2) / Math.pow(dataSize/30, 2)) )
    }

    /**
     * 海の深さ, モデル海洋の幅をセットする
     *
     * @param depth 深さ(m) Number
     * @param width モデル海洋の幅(km) Number
     */
    @Override
    public void setDepth(Object ... args) throws IlleagalArgumentException {
        // 引数チェック
        if(args.length == 2 && args[0] instanceof Number && args[1] instanceof Number) {
            depth = (double)args[0];
            width = (double)args[1];
        } else {
            throw new IlleagalArgumentException();
        }

        // xセット
        for(int idx = 0; idx < dataSize; ++ idx)
            x[idx] = width/dataSize*idx;

        // 計算用変数初期化
        ub = new double[dataSize];
        up = new double[dataSize];
        uf = new double[dataSize];
        zb = new double[dataSize];
        zp = new double[dataSize];
        zf = new double[dataSize];
        dx = width/dataSize;
        dt = 1;
        status = TsunamiSimulator.READY;
    }

    @Override
    protected void step() {

    }

}