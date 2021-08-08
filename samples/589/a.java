import org.datavec.api.records.Record;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.transform.TransformProcess;

class TransformProcessRecordReader implements RecordReader {
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

    protected Record next;
    protected RecordReader recordReader;
    protected TransformProcess transformProcess;

}

