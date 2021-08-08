import org.ansj.app.crf.pojo.Element;
import java.util.List;

class Config {
    /**
     * 得到一个位置的所有特征
     * 
     * @param list
     * @param index
     * @return KeyValue(词语,featureLength*tagNum)
     */
    public char[][] makeFeatureArr(List&lt;Element&gt; list, int index) {
	char[][] result = new char[template.length][];
	char[] chars = null;
	int len = 0;
	int i = 0;
	for (; i &lt; template.length; i++) {
	    if (template[i].length == 0) {
		continue;
	    }
	    chars = new char[template[i].length + 1];
	    len = chars.length - 1;
	    for (int j = 0; j &lt; len; j++) {
		chars[j] = getNameIfOutArr(list, index + template[i][j]);
	    }
	    chars[len] = (char) (FEATURE_BEGIN + i);
	    result[i] = chars;
	}

	return result;
    }

    private int[][] template = { { -2 }, { -1 }, { 0 }, { 1 }, { 2 }, { -2, -1 }, { -1, 0 }, { 0, 1 }, { 1, 2 },
	    { -1, 1 } };
    public static final char FEATURE_BEGIN = 150;
    public static final char BEGIN = 128;
    public static final char END = 129;

    public char getNameIfOutArr(List&lt;Element&gt; list, int index) {
	if (index &lt; 0) {
	    return Config.BEGIN;
	} else if (index &gt;= list.size()) {
	    return Config.END;
	} else {
	    return list.get(index).name;
	}
    }

}

