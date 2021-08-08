import java.util.Random;
import org.apache.commons.math4.PerfTestUtils;

class RandomNumberGeneratorBenchmark {
    /**
     * Test all generators.
     * The reference is JDK's "Random".
     *
     * @param list List of generators to benchmark.
     */
    public void benchmark(List&lt;UniformRandomProvider&gt; rngList) {
	// Reference is JDK's "Random".
	final Random jdk = new Random();

	// List of benchmarked codes.
	final PerfTestUtils.RunTest[] candidates = new PerfTestUtils.RunTest[rngList.size() + 1];

	// "nextInt()" benchmark.

	candidates[0] = new PerfTestUtils.RunTest(jdk.toString()) {
	    @Override
	    public Double call() throws Exception {
		return (double) jdk.nextInt();
	    }
	};
	for (int i = 0; i &lt; rngList.size(); i++) {
	    final UniformRandomProvider rng = rngList.get(i);

	    candidates[i + 1] = new PerfTestUtils.RunTest(rng.toString()) {
		@Override
		public Double call() throws Exception {
		    return (double) rng.nextInt();
		}
	    };
	}

	PerfTestUtils.timeAndReport("nextInt()", MAX_NAME_WIDTH, NUM_CALLS, NUM_STATS, false, candidates);

	// "nextDouble()" benchmark.

	candidates[0] = new PerfTestUtils.RunTest(jdk.toString()) {
	    @Override
	    public Double call() throws Exception {
		return (double) jdk.nextDouble();
	    }
	};
	for (int i = 0; i &lt; rngList.size(); i++) {
	    final UniformRandomProvider rng = rngList.get(i);

	    candidates[i + 1] = new PerfTestUtils.RunTest(rng.toString()) {
		@Override
		public Double call() throws Exception {
		    return rng.nextDouble();
		}
	    };
	}

	PerfTestUtils.timeAndReport("nextDouble()", MAX_NAME_WIDTH, NUM_CALLS, NUM_STATS, false, candidates);

	// "nextLong()" benchmark.

	candidates[0] = new PerfTestUtils.RunTest(jdk.toString()) {
	    @Override
	    public Double call() throws Exception {
		return (double) jdk.nextLong();
	    }
	};
	for (int i = 0; i &lt; rngList.size(); i++) {
	    final UniformRandomProvider rng = rngList.get(i);

	    candidates[i + 1] = new PerfTestUtils.RunTest(rng.toString()) {
		@Override
		public Double call() throws Exception {
		    return (double) rng.nextLong();
		}
	    };
	}

	PerfTestUtils.timeAndReport("nextLong()", MAX_NAME_WIDTH, NUM_CALLS, NUM_STATS, false, candidates);
    }

    /** Report formatting. */
    private static final int MAX_NAME_WIDTH = 45;
    /** Number of loops over the operations to be benchmarked. */
    private static final int NUM_CALLS = 1_000_000;
    /** Number of runs for computing the statistics. */
    private static final int NUM_STATS = 500;

}

