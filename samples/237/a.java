import java.util.Map;

class ExportPdfPapers {
    /**
     * 
     * @return the names of the given paper formats
     */
    String[] getPaperNames() {
	Object[] o_names = paperFormats.keySet().toArray();
	String[] names = new String[o_names.length];
	for (int i = 0; i &lt; paperFormats.size(); i++) {
	    names[i] = (String) o_names[i];
	}
	return names;
    }

    /**
     * map to store all papers
     */
    Map&lt;String, Paper&gt; paperFormats = new HashMap&lt;&gt;();

}

