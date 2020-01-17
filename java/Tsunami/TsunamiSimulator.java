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
        step = 0;
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
