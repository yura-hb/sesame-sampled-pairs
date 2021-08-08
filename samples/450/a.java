import org.apache.commons.math4.exception.MathIllegalStateException;
import org.apache.commons.math4.exception.util.LocalizedFormats;
import org.apache.commons.math4.util.ResizableDoubleArray;

class DescriptiveStatistics implements StatisticalSummary, Serializable {
    /**
     * Removes the most recent value from the dataset.
     *
     * @throws MathIllegalStateException if there are no elements stored
     */
    public void removeMostRecentValue() throws MathIllegalStateException {
	try {
	    eDA.discardMostRecentElements(1);
	} catch (MathIllegalArgumentException ex) {
	    throw new MathIllegalStateException(LocalizedFormats.NO_DATA);
	}
    }

    /** Stored data values. */
    private ResizableDoubleArray eDA = new ResizableDoubleArray();

}

