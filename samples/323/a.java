import org.datavec.api.io.labels.PathLabelGenerator;
import java.io.*;
import java.util.*;

abstract class BaseImageRecordReader extends BaseRecordReader {
    /**
     * Accumulate the label from the path
     *
     * @param path the path to get the label from
     */
    protected void accumulateLabel(String path) {
	String name = getLabel(path);
	if (!labels.contains(name))
	    labels.add(name);
    }

    protected List&lt;String&gt; labels = new ArrayList&lt;&gt;();
    protected PathLabelGenerator labelGenerator = null;
    protected Map&lt;String, String&gt; fileNameMap = new LinkedHashMap&lt;&gt;();

    /**
     * Get the label from the given path
     *
     * @param path the path to get the label from
     * @return the label for the given path
     */
    public String getLabel(String path) {
	if (labelGenerator != null) {
	    return labelGenerator.getLabelForPath(path).toString();
	}
	if (fileNameMap != null && fileNameMap.containsKey(path))
	    return fileNameMap.get(path);
	return (new File(path)).getParentFile().getName();
    }

}

