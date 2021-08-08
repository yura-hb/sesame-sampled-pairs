import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import java.util.List;

abstract class BaseDataFetcher implements DataSetFetcher {
    /**
     * Initializes this data transform fetcher from the passed in datasets
     *
     * @param examples the examples to use
     */
    protected void initializeCurrFromList(List&lt;DataSet&gt; examples) {

	if (examples.isEmpty())
	    log.warn("Warning: empty dataset from the fetcher");

	INDArray inputs = createInputMatrix(examples.size());
	INDArray labels = createOutputMatrix(examples.size());
	for (int i = 0; i &lt; examples.size(); i++) {
	    inputs.putRow(i, examples.get(i).getFeatures());
	    labels.putRow(i, examples.get(i).getLabels());
	}
	curr = new DataSet(inputs, labels);

    }

    protected static final Logger log = LoggerFactory.getLogger(BaseDataFetcher.class);
    protected DataSet curr;
    protected int inputColumns = -1;
    protected int numOutcomes = -1;

    /**
     * Creates a feature vector
     *
     * @param numRows the number of examples
     * @return a feature vector
     */
    protected INDArray createInputMatrix(int numRows) {
	return Nd4j.create(numRows, inputColumns);
    }

    protected INDArray createOutputMatrix(int numRows) {
	return Nd4j.create(numRows, numOutcomes);
    }

}

