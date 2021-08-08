import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

class DistributionStats implements NormalizerStats {
    class Builder implements Builder&lt;DistributionStats&gt; {
	/**
	 * Create a DistributionStats object from the data ingested so far. Can be used multiple times when updating
	 * online.
	 */
	public DistributionStats build() {
	    if (runningMean == null) {
		throw new RuntimeException("No data was added, statistics cannot be determined");
	    }
	    return new DistributionStats(runningMean.dup(), Transforms.sqrt(runningVariance, true));
	}

	private INDArray runningMean;
	private INDArray runningVariance;

    }

    private static final Logger logger = LoggerFactory.getLogger(NormalizerStandardize.class);
    private final INDArray mean;
    private final INDArray std;

    /**
     * @param mean row vector of means
     * @param std  row vector of standard deviations
     */
    public DistributionStats(@NonNull INDArray mean, @NonNull INDArray std) {
	Transforms.max(std, Nd4j.EPS_THRESHOLD, false);
	if (std.min(1) == Nd4j.scalar(Nd4j.EPS_THRESHOLD)) {
	    logger.info("API_INFO: Std deviation found to be zero. Transform will round up to epsilon to avoid nans.");
	}

	this.mean = mean;
	this.std = std;
    }

}

