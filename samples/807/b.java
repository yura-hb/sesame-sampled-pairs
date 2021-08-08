import org.datavec.api.writable.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import java.util.ArrayList;
import java.util.List;

class RecordConverter {
    /**
     * Convert a set of records in to a matrix
     * @param matrix the records ot convert
     * @return the matrix for the records
     */
    public static List&lt;List&lt;Writable&gt;&gt; toRecords(INDArray matrix) {
	List&lt;List&lt;Writable&gt;&gt; ret = new ArrayList&lt;&gt;();
	for (int i = 0; i &lt; matrix.rows(); i++) {
	    ret.add(RecordConverter.toRecord(matrix.getRow(i)));
	}

	return ret;
    }

    /**
     * Convert an ndarray to a record
     * @param array the array to convert
     * @return the record
     */
    public static List&lt;Writable&gt; toRecord(INDArray array) {
	List&lt;Writable&gt; writables = new ArrayList&lt;&gt;();
	writables.add(new NDArrayWritable(array));
	return writables;
    }

}

