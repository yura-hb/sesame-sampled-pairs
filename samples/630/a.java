import java.util.ArrayList;
import java.util.List;

class BatchCSVRecord implements Serializable {
    /**
     * Create a batch csv record
     * from a list of writables.
     * @param batch
     * @return
     */
    public static BatchCSVRecord fromWritables(List&lt;List&lt;Writable&gt;&gt; batch) {
	List&lt;SingleCSVRecord&gt; records = new ArrayList&lt;&gt;(batch.size());
	for (List&lt;Writable&gt; list : batch) {
	    List&lt;String&gt; add = new ArrayList&lt;&gt;(list.size());
	    for (Writable writable : list) {
		add.add(writable.toString());
	    }
	    records.add(new SingleCSVRecord(add));
	}

	return BatchCSVRecord.builder().records(records).build();
    }

}

