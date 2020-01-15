package Tsunami;

import java.util.Iterator;

abstract public class TsunamiSimulator implements Iterable<StepData>, Iterator<StepData>{

    // 定数
    private static final int H = 60*60;
    private static final int M = 60;
    private static final int S = 1;
    public static final double grav = 9.8;
    public static final double eps = 0.01;

    // ステータス
    protected int status = 0;
    public static final int READY = 0;
    public static final int RUNNING = 1;
    public static final int ERROR = 2;

    // 時間データ
    private int clock = 0*H + 0*M + 0*S;
    private int timeEnd = 3*H, itrTimeStep = 1*M;

    // 計算用変数
    protected int step, dataSize;
    protected double dx, dt;
    protected double ub[], up[], uf[];    // 水平流速
    protected double zb[], zp[], zf[];    // 海面変位
    protected double x[], depth[];        // 位置(m)、深さ(m)

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
        for(int idx = 0; idx < (int)((double)itrTimeStep/dt); ++ idx) {
            step();
            ++ step;
        }
        clock += itrTimeStep;
        return sdata;
    }

    /**
     * 計算用変数を初期化する
     * (depth, x以外)
     */
    protected void initVariables(int dataSize) {
        this.dataSize = dataSize;
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
     * 地形データを読み込んでx, depthにセットする
     *
     * @param Object... 継承先で定義
     * @throws IllegalArgumentException 引数の方が想定と異なる場合投げる
     */
    abstract public void setDepth(Object ... args) throws IllegalArgumentException;

    /**
     * シミュレータを1ステップ進める
     * 継承先でオーバーライドする
    */
    abstract protected void step();

}
