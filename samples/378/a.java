import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.math4.exception.ZeroException;
import org.apache.commons.math4.exception.util.LocalizedFormats;
import org.apache.commons.math4.stat.descriptive.SummaryStatistics;
import org.apache.commons.math4.util.MathUtils;

class EmpiricalDistribution extends AbstractRealDistribution implements ContinuousDistribution {
    /**
     * Computes the empirical distribution using data read from a URL.
     *
     * &lt;p&gt;The input file &lt;i&gt;must&lt;/i&gt; be an ASCII text file containing one
     * valid numeric entry per line.&lt;/p&gt;
     *
     * @param url url of the input file
     *
     * @throws IOException if an IO error occurs
     * @throws NullArgumentException if url is null
     * @throws ZeroException if URL contains no data
     */
    public void load(URL url) throws IOException, NullArgumentException, ZeroException {
	MathUtils.checkNotNull(url);
	Charset charset = Charset.forName(FILE_CHARSET);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), charset));
	try {
	    DataAdapter da = new StreamDataAdapter(in);
	    da.computeStats();
	    if (sampleStats.getN() == 0) {
		throw new ZeroException(LocalizedFormats.URL_CONTAINS_NO_DATA, url);
	    }
	    // new adapter for the second pass
	    in = new BufferedReader(new InputStreamReader(url.openStream(), charset));
	    fillBinStats(new StreamDataAdapter(in));
	    loaded = true;
	} finally {
	    try {
		in.close();
	    } catch (IOException ex) { //NOPMD
		// ignore
	    }
	}
    }

    /** Character set for file input */
    private static final String FILE_CHARSET = "US-ASCII";
    /** Sample statistics */
    private SummaryStatistics sampleStats = null;
    /** is the distribution loaded? */
    private boolean loaded = false;
    /** Min loaded value */
    private double min = Double.POSITIVE_INFINITY;
    /** Max loaded value */
    private double max = Double.NEGATIVE_INFINITY;
    /** Grid size */
    private double delta = 0d;
    /** number of bins */
    private final int binCount;
    /** List of SummaryStatistics objects characterizing the bins */
    private final List&lt;SummaryStatistics&gt; binStats;
    /** upper bounds of subintervals in (0,1) "belonging" to the bins */
    private double[] upperBounds = null;

    /**
     * Fills binStats array (second pass through data file).
     *
     * @param da object providing access to the data
     * @throws IOException  if an IO error occurs
     */
    private void fillBinStats(final DataAdapter da) throws IOException {
	// Set up grid
	min = sampleStats.getMin();
	max = sampleStats.getMax();
	delta = (max - min) / binCount;

	// Initialize binStats ArrayList
	if (!binStats.isEmpty()) {
	    binStats.clear();
	}
	for (int i = 0; i &lt; binCount; i++) {
	    SummaryStatistics stats = new SummaryStatistics();
	    binStats.add(i, stats);
	}

	// Filling data in binStats Array
	da.computeBinStats();

	// Assign upperBounds based on bin counts
	upperBounds = new double[binCount];
	upperBounds[0] = ((double) binStats.get(0).getN()) / (double) sampleStats.getN();
	for (int i = 1; i &lt; binCount - 1; i++) {
	    upperBounds[i] = upperBounds[i - 1] + ((double) binStats.get(i).getN()) / (double) sampleStats.getN();
	}
	upperBounds[binCount - 1] = 1.0d;
    }

    class StreamDataAdapter extends DataAdapter {
	/** Character set for file input */
	private static final String FILE_CHARSET = "US-ASCII";
	/** Sample statistics */
	private SummaryStatistics sampleStats = null;
	/** is the distribution loaded? */
	private boolean loaded = false;
	/** Min loaded value */
	private double min = Double.POSITIVE_INFINITY;
	/** Max loaded value */
	private double max = Double.NEGATIVE_INFINITY;
	/** Grid size */
	private double delta = 0d;
	/** number of bins */
	private final int binCount;
	/** List of SummaryStatistics objects characterizing the bins */
	private final List&lt;SummaryStatistics&gt; binStats;
	/** upper bounds of subintervals in (0,1) "belonging" to the bins */
	private double[] upperBounds = null;

	/**
	 * Create a StreamDataAdapter from a BufferedReader
	 *
	 * @param in BufferedReader input stream
	 */
	StreamDataAdapter(BufferedReader in) {
	    super();
	    inputStream = in;
	}

    }

    abstract class DataAdapter {
	/** Character set for file input */
	private static final String FILE_CHARSET = "US-ASCII";
	/** Sample statistics */
	private SummaryStatistics sampleStats = null;
	/** is the distribution loaded? */
	private boolean loaded = false;
	/** Min loaded value */
	private double min = Double.POSITIVE_INFINITY;
	/** Max loaded value */
	private double max = Double.NEGATIVE_INFINITY;
	/** Grid size */
	private double delta = 0d;
	/** number of bins */
	private final int binCount;
	/** List of SummaryStatistics objects characterizing the bins */
	private final List&lt;SummaryStatistics&gt; binStats;
	/** upper bounds of subintervals in (0,1) "belonging" to the bins */
	private double[] upperBounds = null;

	/**
	 * Compute sample statistics.
	 *
	 * @throws IOException if an error occurs computing sample stats
	 */
	public abstract void computeStats() throws IOException;

	/**
	 * Compute bin stats.
	 *
	 * @throws IOException  if an error occurs computing bin stats
	 */
	public abstract void computeBinStats() throws IOException;

    }

}

