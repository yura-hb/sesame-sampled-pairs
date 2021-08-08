import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.sequencevectors.interfaces.SequenceElementFactory;
import org.deeplearning4j.models.sequencevectors.sequence.SequenceElement;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import java.io.*;
import java.nio.charset.StandardCharsets;

class WordVectorSerializer {
    /**
     * This method saves specified SequenceVectors model to target  file
     *
     * @param vectors SequenceVectors model
     * @param factory SequenceElementFactory implementation for your objects
     * @param file Target output file
     * @param &lt;T&gt;
     */
    public static &lt;T extends SequenceElement&gt; void writeSequenceVectors(@NonNull SequenceVectors&lt;T&gt; vectors,
	    @NonNull SequenceElementFactory&lt;T&gt; factory, @NonNull File file) throws IOException {
	try (FileOutputStream fos = new FileOutputStream(file)) {
	    writeSequenceVectors(vectors, factory, fos);
	}
    }

    /**
     * This method saves specified SequenceVectors model to target  OutputStream
     *
     * @param vectors SequenceVectors model
     * @param factory SequenceElementFactory implementation for your objects
     * @param stream Target output stream
     * @param &lt;T&gt;
     */
    public static &lt;T extends SequenceElement&gt; void writeSequenceVectors(@NonNull SequenceVectors&lt;T&gt; vectors,
	    @NonNull SequenceElementFactory&lt;T&gt; factory, @NonNull OutputStream stream) throws IOException {
	WeightLookupTable&lt;T&gt; lookupTable = vectors.getLookupTable();
	VocabCache&lt;T&gt; vocabCache = vectors.getVocab();

	try (PrintWriter writer = new PrintWriter(
		new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8)))) {

	    // at first line we save VectorsConfiguration
	    writer.write(vectors.getConfiguration().toEncodedJson());

	    // now we have elements one by one
	    for (int x = 0; x &lt; vocabCache.numWords(); x++) {
		T element = vocabCache.elementAtIndex(x);
		String json = factory.serialize(element);
		INDArray d = Nd4j.create(1);
		double[] vector = lookupTable.vector(element.getLabel()).dup().data().asDouble();
		ElementPair pair = new ElementPair(json, vector);
		writer.println(pair.toEncodedJson());
		writer.flush();
	    }
	}
    }

    class ElementPair {
	/**
	 * This utility method serializes ElementPair into JSON + packs it into Base64-encoded string
	 *
	 * @return
	 */
	protected String toEncodedJson() {
	    ObjectMapper mapper = SequenceElement.mapper();
	    Base64 base64 = new Base64(Integer.MAX_VALUE);
	    try {
		String json = mapper.writeValueAsString(this);
		String output = base64.encodeAsString(json.getBytes("UTF-8"));
		return output;
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}

    }

}

