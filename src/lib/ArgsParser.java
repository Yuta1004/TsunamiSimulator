package lib;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ArgsParser
 * 引数をパースして扱いやすくする
 */
public class ArgsParser {

    private String args[];
    private ArrayList<String> outOfGroup;
    private HashMap<String, String> matchTable;

    /**
     * コンストラクタ
     */
    public ArgsParser(String[] args) {
        this.args = args;
        outOfGroup = new ArrayList<String>();
        matchTable = new HashMap<String, String>();
        parse();
    }

    /**
     * 指定タグがargs内に存在していたかを返す
     *
     * @param tag タグ
     */
    public boolean hasTag(String tag) {
        return matchTable.containsKey(tag);
    }

    /**
     * 指定タグに対応する値を返す
     * 存在しない場合は空文字を返す
     */
    public String getValue(String tag) {
        String value = matchTable.get(tag);
        if(value == null)
            return "";
        return value;
    }

    /**
     * タグに紐付けられなかった値の配列を返す
     */
    public String[] getNonMappedValues() {
        return outOfGroup.toArray(String[]::new);
    }

    /**
     * argsのパースを行う
     */
    private void parse() {
        for(int idx = 0; idx < args.length; ++ idx) {
            // fetch
            String arg = args[idx], next = "";
            if(idx < args.length-1)
                next = args[idx+1];

            // parse
            if(checkPrefix(arg)) {
                if(checkPrefix(next)) {
                    matchTable.put(exclusionPrefix(arg), "");
                } else {
                    matchTable.put(exclusionPrefix(arg), next);
                    ++ idx;
                }
            } else {
                outOfGroup.add(arg);
            }
        }
    }

    /**
     * 接頭語がパース対象のものかチェック
     *
     * @param target 検証対象文字列
     */
    private boolean checkPrefix(String target) {
        String prefixs[] = {"--", "-"};
        for(String prefix: prefixs) {
            if(target.startsWith(prefix))
                return true;
        }
        return false;
    }

    /**
     * 接頭語を除いた文字列を返す
     *
     * @param target 対象文字列
     */
    private String exclusionPrefix(String target) {
        String prefixs[] = {"--", "-"};
        for(String prefix: prefixs) {
            if(target.startsWith(prefix))
                return target.substring(prefix.length(), target.length());
        }
        return "";
    }

}
