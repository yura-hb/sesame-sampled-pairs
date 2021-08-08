import org.nd4j.linalg.dataset.api.preprocessor.Normalizer;
import java.io.*;

class NormalizerSerializer {
    /**
     * Serialize a normalizer to an output stream
     *
     * @param normalizer the normalizer
     * @param stream     the output stream to write to
     * @throws IOException
     */
    public void write(@NonNull Normalizer normalizer, @NonNull OutputStream stream) throws IOException {
	NormalizerSerializerStrategy strategy = getStrategy(normalizer);

	writeHeader(stream, Header.fromStrategy(strategy));
	//noinspection unchecked
	strategy.write(normalizer, stream);
    }

    private List&lt;NormalizerSerializerStrategy&gt; strategies = new ArrayList&lt;&gt;();
    private static final String HEADER = "NORMALIZER";

    /**
     * Get a serializer strategy the given normalizer
     *
     * @param normalizer the normalizer to find a compatible serializer strategy for
     * @return the compatible strategy
     */
    private NormalizerSerializerStrategy getStrategy(Normalizer normalizer) {
	for (NormalizerSerializerStrategy strategy : strategies) {
	    if (strategySupportsNormalizer(strategy, normalizer.getType(), normalizer.getClass())) {
		return strategy;
	    }
	}
	throw new RuntimeException(String.format(
		"No serializer strategy found for normalizer of class %s. If this is a custom normalizer, you probably "
			+ "forgot to register a corresponding custom serializer strategy with this serializer.",
		normalizer.getClass()));
    }

    /**
     * Write the data header
     *
     * @param stream the output stream
     * @param header the header to write
     * @throws IOException
     */
    private void writeHeader(OutputStream stream, Header header) throws IOException {
	DataOutputStream dos = new DataOutputStream(stream);
	dos.writeUTF(HEADER);

	// Write the current version
	dos.writeInt(1);

	// Write the normalizer opType
	dos.writeUTF(header.normalizerType.toString());

	// If the header contains a custom class opName, write that too
	if (header.customStrategyClass != null) {
	    dos.writeUTF(header.customStrategyClass.getName());
	}
    }

    /**
     * Check if a serializer strategy supports a normalizer. If the normalizer is a custom opType, it checks if the
     * supported normalizer class matches.
     *
     * @param strategy
     * @param normalizerType
     * @param normalizerClass
     * @return whether the strategy supports the normalizer
     */
    private boolean strategySupportsNormalizer(NormalizerSerializerStrategy strategy, NormalizerType normalizerType,
	    Class&lt;? extends Normalizer&gt; normalizerClass) {
	if (!strategy.getSupportedType().equals(normalizerType)) {
	    return false;
	}
	if (strategy.getSupportedType().equals(NormalizerType.CUSTOM)) {
	    // Strategy should be instance of CustomSerializerStrategy
	    if (!(strategy instanceof CustomSerializerStrategy)) {
		throw new IllegalArgumentException(
			"Strategies supporting CUSTOM opType must be instance of CustomSerializerStrategy, got"
				+ strategy.getClass());
	    }
	    return ((CustomSerializerStrategy) strategy).getSupportedClass().equals(normalizerClass);
	}
	return true;
    }

    class Header {
	private List&lt;NormalizerSerializerStrategy&gt; strategies = new ArrayList&lt;&gt;();
	private static final String HEADER = "NORMALIZER";

	public static Header fromStrategy(NormalizerSerializerStrategy strategy) {
	    if (strategy instanceof CustomSerializerStrategy) {
		return new Header(strategy.getSupportedType(), strategy.getClass());
	    } else {
		return new Header(strategy.getSupportedType(), null);
	    }
	}

    }

}

