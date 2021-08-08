import org.datavec.api.transform.transform.BaseColumnsMathOpTransform;
import java.util.List;

class CoordinatesDistanceTransform extends BaseColumnsMathOpTransform {
    /**
     * Transform an object
     * in to another object
     *
     * @param input the record to transform
     * @return the transformed writable
     */
    @Override
    public Object map(Object input) {
	List row = (List) input;
	String[] first = row.get(0).toString().split(delimiter);
	String[] second = row.get(1).toString().split(delimiter);
	String[] stdev = columns.length &gt; 2 ? row.get(2).toString().split(delimiter) : null;

	double dist = 0;
	for (int i = 0; i &lt; first.length; i++) {
	    double d = Double.parseDouble(first[i]) - Double.parseDouble(second[i]);
	    double s = stdev != null ? Double.parseDouble(stdev[i]) : 1;
	    dist += (d * d) / (s * s);
	}
	return Math.sqrt(dist);
    }

    protected String delimiter = DEFAULT_DELIMITER;

}

