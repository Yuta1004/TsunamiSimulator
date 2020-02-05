# TsunamiSimulator

津波シミュレータ Java

## コマンド

```
// 実行
make run

// dist生成(win)
make dist-win JMODS_PATH=path

// dist生成(macos)
make dist-win JMODS_PATH=path

// dist生成(linux)
make dist-linux JMODS_PATH=path
```

## 環境変数

以下の環境変数を設定してください  
makeの引数として設定することも可能です  

### JAVAFX_PATH

javafx-sdkのルートパス

> 正 : /path/to/javafx/sdk/
> 誤 : /path/to/javafx/sdk/lib

### JAVA_HOME

jdkのパス

> 正 : /path/to/jdk-x.x.x/Contents/Home/

## 使い方

### jar

1. dist/ に入る
2. `make` or `runtime/bin/java -jar TsunamiSimulator.jar` を叩く
2. 地形データファイルを選択
3. **Init** ボタンを押す
4. **Start** ボタンを押す

- **Stop**ボタン : シミュレート停止
- **Step** ボタン : 1ステップずつシミュレート

