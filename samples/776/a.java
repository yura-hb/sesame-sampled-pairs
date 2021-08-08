import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.reader.impl.BasicModelUtils;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.sequencevectors.sequence.SequenceElement;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class WordVectorSerializer {
    /**
     * Restores previously serialized ParagraphVectors model
     *
     * Deprecation note: Please, consider using readParagraphVectors() method instead
     *
     * @param file File that contains previously serialized model
     * @return
     * @deprecated Use readParagraphVectors() method instead
     */
    @Deprecated
    public static ParagraphVectors readParagraphVectorsFromText(@NonNull File file) {
	try (FileInputStream fis = new FileInputStream(file)) {
	    return readParagraphVectorsFromText(fis);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private static final String WHITESPACE_REPLACEMENT = "_Az92_";

    /**
     * Restores previously serialized ParagraphVectors model
     *
     * Deprecation note: Please, consider using readParagraphVectors() method instead
     *
     * @param stream InputStream that contains previously serialized model
     * @return
     * @deprecated Use readParagraphVectors() method instead
     */
    @Deprecated
    public static ParagraphVectors readParagraphVectorsFromText(@NonNull InputStream stream) {
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
	    ArrayList&lt;String&gt; labels = new ArrayList&lt;&gt;();
	    ArrayList&lt;INDArray&gt; arrays = new ArrayList&lt;&gt;();
	    VocabCache&lt;VocabWord&gt; vocabCache = new AbstractCache.Builder&lt;VocabWord&gt;().build();
	    String line = "";
	    while ((line = reader.readLine()) != null) {
		String[] split = line.split(" ");
		split[1] = split[1].replaceAll(WHITESPACE_REPLACEMENT, " ");
		VocabWord word = new VocabWord(1.0, split[1]);
		if (split[0].equals("L")) {
		    // we have label element here
		    word.setSpecial(true);
		    word.markAsLabel(true);
		    labels.add(word.getLabel());
		} else if (split[0].equals("E")) {
		    // we have usual element, aka word here
		    word.setSpecial(false);
		    word.markAsLabel(false);
		} else
		    throw new IllegalStateException(
			    "Source stream doesn't looks like ParagraphVectors serialized model");

		// this particular line is just for backward compatibility with InMemoryLookupCache
		word.setIndex(vocabCache.numWords());

		vocabCache.addToken(word);
		vocabCache.addWordToIndex(word.getIndex(), word.getLabel());

		// backward compatibility code
		vocabCache.putVocabWord(word.getLabel());

		float[] vector = new float[split.length - 2];

		for (int i = 2; i &lt; split.length; i++) {
		    vector[i - 2] = Float.parseFloat(split[i]);
		}

		INDArray row = Nd4j.create(vector);

		arrays.add(row);
	    }

	    // now we create syn0 matrix, using previously fetched rows
	    /*INDArray syn = Nd4j.create(new int[]{arrays.size(), arrays.get(0).columns()});
	    for (int i = 0; i &lt; syn.rows(); i++) {
	        syn.putRow(i, arrays.get(i));
	    }*/
	    INDArray syn = Nd4j.vstack(arrays);

	    InMemoryLookupTable&lt;VocabWord&gt; lookupTable = (InMemoryLookupTable&lt;VocabWord&gt;) new InMemoryLookupTable.Builder&lt;VocabWord&gt;()
		    .vectorLength(arrays.get(0).columns()).useAdaGrad(false).cache(vocabCache).build();
	    Nd4j.clearNans(syn);
	    lookupTable.setSyn0(syn);

	    LabelsSource source = new LabelsSource(labels);
	    ParagraphVectors vectors = new ParagraphVectors.Builder().labelsSource(source).vocabCache(vocabCache)
		    .lookupTable(lookupTable).modelUtils(new BasicModelUtils&lt;VocabWord&gt;()).build();

	    try {
		reader.close();
	    } catch (Exception e) {
	    }

	    vectors.extractLabels();

	    return vectors;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

}

