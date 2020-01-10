import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TsunamiSimulator {

    // 定数
    static final double grav = 9.8;

    // その他
    int dataSize = -1;
    double dx, dt;
    double ub[], up[], uf[];    // 水平流速
    double zb[], zp[], zf[];    // 海面変位
    double x[], depth[];        // 位置(m)、深さ(m)

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

    private void error(String msg) {
        System.err.println("[ERROR] "+ msg);
        System.exit(0);
    }

}
