import org.ansj.dic.PathToStream;
import org.ansj.domain.KV;
import org.ansj.util.MyStaticValue;
import java.io.BufferedReader;
import java.util.Map;

class AmbiguityLibrary {
    /**
     * 插入到树种
     * 
     * @param key
     * @param value
     */
    public static void insert(String key, Value value) {
	Forest forest = get(key);
	Library.insertWord(forest, value);
    }

    private static final Map&lt;String, KV&lt;String, Forest&gt;&gt; AMBIGUITY = new HashMap&lt;&gt;();
    private static final Log LOG = MyStaticValue.getLog(AmbiguityLibrary.class);

    /**
     * 根据key获取
     * 
     */
    public static Forest get(String key) {

	KV&lt;String, Forest&gt; kv = AMBIGUITY.get(key);

	if (kv == null) {
	    if (MyStaticValue.ENV.containsKey(key)) {
		putIfAbsent(key, MyStaticValue.ENV.get(key));
		return get(key);
	    }

	    LOG.warn("crf " + key + " not found in config ");
	    return null;
	}

	Forest sw = kv.getV();
	if (sw == null) {
	    try {
		sw = init(key, kv, false);
	    } catch (Exception e) {
	    }
	}
	return sw;
    }

    public static void putIfAbsent(String key, String path) {
	if (!AMBIGUITY.containsKey(key)) {
	    AMBIGUITY.put(key, KV.with(path, (Forest) null));
	}
    }

    /**
     * 加载
     * 
     * @return
     */
    private static synchronized Forest init(String key, KV&lt;String, Forest&gt; kv, boolean reload) {
	Forest forest = kv.getV();
	if (forest != null) {
	    if (reload) {
		forest.clear();
	    } else {
		return forest;
	    }
	} else {
	    forest = new Forest();
	}
	try (BufferedReader br = IOUtil.getReader(PathToStream.stream(kv.getK()), "utf-8")) {
	    String temp;
	    LOG.debug("begin init ambiguity");
	    long start = System.currentTimeMillis();
	    while ((temp = br.readLine()) != null) {
		if (StringUtil.isNotBlank(temp)) {
		    temp = StringUtil.trim(temp);
		    String[] split = temp.split("\t");
		    StringBuilder sb = new StringBuilder();
		    if (split.length % 2 != 0) {
			LOG.error("init ambiguity  error in line :" + temp + " format err !");
			continue;
		    }
		    for (int i = 0; i &lt; split.length; i += 2) {
			sb.append(split[i]);
		    }
		    forest.addBranch(sb.toString(), split);
		}
	    }
	    LOG.info("load dic use time:" + (System.currentTimeMillis() - start) + " path is : " + kv.getK());
	    kv.setV(forest);
	    return forest;
	} catch (Exception e) {
	    LOG.error("Init ambiguity library error :" + e.getMessage() + ", path: " + kv.getK());
	    AMBIGUITY.remove(key);
	    return null;
	}
    }

}

