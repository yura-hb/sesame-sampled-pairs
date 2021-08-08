import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

class StandardScaler {
    /**
     * Fit the given model
     * @param iterator the data to iterate oer
     */
    public void fit(DataSetIterator iterator) {
	while (iterator.hasNext()) {
	    DataSet next = iterator.next();
	    runningTotal += next.numExamples();
	    batchCount = next.getFeatures().size(0);
	    if (mean == null) {
		//start with the mean and std of zero
		//column wise
		mean = next.getFeatures().mean(0);
		std = (batchCount == 1) ? Nd4j.zeros(mean.shape()) : Transforms.pow(next.getFeatures().std(0), 2);
		std.muli(batchCount);
	    } else {
		// m_newM = m_oldM + (x - m_oldM)/m_n;
		// This only works if batch size is 1, m_newS = m_oldS + (x - m_oldM)*(x - m_newM);
		INDArray xMinusMean = next.getFeatures().subRowVector(mean);
		INDArray newMean = mean.add(xMinusMean.sum(0).divi(runningTotal));
		// Using http://i.stanford.edu/pub/cstr/reports/cs/tr/79/773/CS-TR-79-773.pdf
		// for a version of calc variance when dataset is partitioned into two sample sets
		// Also described in https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm
		// delta = mean_B - mean_A; A is data seen so far, B is the current batch
		// M2 is the var*n
		// M2 = M2_A + M2_B + delta^2 * nA * nB/(nA+nB)
		INDArray meanB = next.getFeatures().mean(0);
		INDArray deltaSq = Transforms.pow(meanB.subRowVector(mean), 2);
		INDArray deltaSqScaled = deltaSq
			.mul(((float) runningTotal - batchCount) * batchCount / (float) runningTotal);
		INDArray mtwoB = Transforms.pow(next.getFeatures().std(0), 2);
		mtwoB.muli(batchCount);
		std = std.add(mtwoB);
		std = std.add(deltaSqScaled);
		mean = newMean;
	    }

	}
	std.divi(runningTotal);
	std = Transforms.sqrt(std);
	std.addi(Nd4j.scalar(Nd4j.EPS_THRESHOLD));
	if (std.min(1) == Nd4j.scalar(Nd4j.EPS_THRESHOLD))
	    logger.info("API_INFO: Std deviation found to be zero. Transform will round upto epsilon to avoid nans.");
	iterator.reset();
    }

    private long runningTotal = 0;
    private long batchCount = 0;
    private INDArray mean, std;
    private INDArray mean, std;
    private static Logger logger = LoggerFactory.getLogger(StandardScaler.class);

}

