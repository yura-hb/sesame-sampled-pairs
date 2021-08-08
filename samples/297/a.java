import org.datavec.api.records.Record;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.transform.TransformProcess;
import java.util.NoSuchElementException;

class TransformProcessRecordReader implements RecordReader {
    /**
     * Get the next record
     *
     * @return
     */
    @Override
    public List&lt;Writable&gt; next() {
	if (!hasNext()) { //Also triggers prefetch
	    throw new NoSuchElementException("No next element");
	}
	List&lt;Writable&gt; out = next.getRecord();
	next = null;
	return out;
    }

    protected Record next;
    protected RecordReader recordReader;
    protected TransformProcess transformProcess;

    /**
     * Whether there are anymore records
     *
     * @return
     */
    @Override
    public boolean hasNext() {
	if (next != null) {
	    return true;
	}
	if (!recordReader.hasNext()) {
	    return false;
	}

	//Prefetch, until we find one that isn't filtered out - or we run out of data
	while (next == null && recordReader.hasNext()) {
	    Record r = recordReader.nextRecord();
	    List&lt;Writable&gt; temp = transformProcess.execute(r.getRecord());
	    if (temp == null) {
		continue;
	    }
	    next = new org.datavec.api.records.impl.Record(temp, r.getMetaData());
	}

	return next != null;
    }

}

