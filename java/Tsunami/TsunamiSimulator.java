package Tsunami;

abstract public class TsunamiSimulator {

    // 定数
    private static final int H = 60*60;
    private static final int M = 60;
    private static final int S = 1;
    public static final double grav = 9.8;
    public static final double eps = 0.01;

    // 時間データ
    private int clock = 0*H + 0*M + 0*S;
    private int itrTimeStep = 1*M;

    // 計算用変数
    protected int step, dataSize;
    protected double dx, dt;
    protected double ub[], up[], uf[];    // 水平流速
    protected double zb[], zp[], zf[];    // 海面変位
    protected double x[], depth[];        // 位置(m)、深さ(m)

    /**
     * 内部時刻をセットする
     *
     * @param hour 時
     * @param min 分
     * @param sec 秒
     */
    public void setClock(int hour, int min, int sec) {
        clock = hour*H + min*M + sec*S;
        if(clock < 0)
            clock += 24*H;
        clock %= 24*H;
    }

    /**
     * 内部時刻を取得する
     */
    public int getClock() {
        return clock;
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
     * シミュレート情報をリセットする
     */
    public void reset() {
        ub = new double[dataSize];
        up = new double[dataSize];
        uf = new double[dataSize];
        zb = new double[dataSize];
        zp = new double[dataSize];
        zf = new double[dataSize];
        step = 0;
    }

    /**
     * シミュレータを1ステップ進める
     */
    public void next() {
        for(int idx = 0; idx < (int)((double)itrTimeStep/dt); ++ idx) {
            step();
            ++ step;
        }
        clock += itrTimeStep;
    }

    /**
     * シミュレータの内部状態を返す
     */
    public StepData getData() {
        return new StepData(clock, step, x, zp, depth);
    }

    /**
     * 地形情報をセットする
     * 継承先でオーバーライドする
     *
     * @param Object... 継承先で定義
     * @throws IllegalArgumentException 引数の方が想定と異なる場合投げる
     */
    abstract public void setDepth(Object ... args) throws IllegalArgumentException;

    /**
     * 指定位置の波の高さをセットする
     * 継承先でオーバーライドする
     *
     * @param pos 距離(km)
     * @param height 高さ(m)
     */
    abstract public void setWaveHeight(int pos, int height);

    /**
     * シミュレータを1ステップ進める
     * 継承先でオーバーライドする
    */
    abstract protected void step();

}
