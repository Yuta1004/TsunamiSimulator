class Main {

    public static void main(String args[]) {
        // 初期設定
        TsunamiSimulator simulator = new TsunamiSimulator("data/DEPTH.data");
        simulator.setClock(12, 0, 0);           // 時計を12:00:00に
        simulator.setSimulateTime(3, 0, 0);     // 3時間分シミュレートする
        simulator.setItrTimeStep(0, 1, 0);      // 1分間隔でデータを取得する
        simulator.setWaveHeight(115, -2);       // 115mの場所に-2mの波
        simulator.setWaveHeight(215, 5);        // 215mの場所に5mの波

        // シミュレート
        for(StepData data: simulator) {
            data.print();
            System.out.println();
        }
    }

}
