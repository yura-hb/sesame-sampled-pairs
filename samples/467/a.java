import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

class DistributionStats implements NormalizerStats {
    /**
     * Load distribution statistics from the file system
     *
     * @param meanFile file containing the means
     * @param stdFile  file containing the standard deviations
     */
    public static DistributionStats load(@NonNull File meanFile, @NonNull File stdFile) throws IOException {
	return new DistributionStats(Nd4j.readBinary(meanFile), Nd4j.readBinary(stdFile));
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

