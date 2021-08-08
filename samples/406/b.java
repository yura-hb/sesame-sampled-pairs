import java.util.*;

class RandomMultiDataSetIterator implements MultiDataSetIterator {
    class Builder {
	/**
	 * Add a new features array to the iterator
	 * @param shape  Shape of the features
	 * @param order  Order ('c' or 'f') for the array
	 * @param values Values to fill the array with
	 */
	public Builder addFeatures(long[] shape, char order, Values values) {
	    features.add(new Triple&lt;&gt;(shape, order, values));
	    return this;
	}

	private List&lt;Triple&lt;long[], Character, Values&gt;&gt; features = new ArrayList&lt;&gt;();

    }

}

