import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.math4.exception.DimensionMismatchException;

class CycleCrossover&lt;T&gt; implements CrossoverPolicy {
    /**
     * Helper for {@link #crossover(Chromosome, Chromosome)}. Performs the actual crossover.
     *
     * @param first the first chromosome
     * @param second the second chromosome
     * @return the pair of new chromosomes that resulted from the crossover
     * @throws DimensionMismatchException if the length of the two chromosomes is different
     */
    protected ChromosomePair mate(final AbstractListChromosome&lt;T&gt; first, final AbstractListChromosome&lt;T&gt; second)
	    throws DimensionMismatchException {

	final int length = first.getLength();
	if (length != second.getLength()) {
	    throw new DimensionMismatchException(second.getLength(), length);
	}

	// array representations of the parents
	final List&lt;T&gt; parent1Rep = first.getRepresentation();
	final List&lt;T&gt; parent2Rep = second.getRepresentation();
	// and of the children: do a crossover copy to simplify the later processing
	final List&lt;T&gt; child1Rep = new ArrayList&lt;&gt;(second.getRepresentation());
	final List&lt;T&gt; child2Rep = new ArrayList&lt;&gt;(first.getRepresentation());

	// the set of all visited indices so far
	final Set&lt;Integer&gt; visitedIndices = new HashSet&lt;&gt;(length);
	// the indices of the current cycle
	final List&lt;Integer&gt; indices = new ArrayList&lt;&gt;(length);

	// determine the starting index
	int idx = randomStart ? GeneticAlgorithm.getRandomGenerator().nextInt(length) : 0;
	int cycle = 1;

	while (visitedIndices.size() &lt; length) {
	    indices.add(idx);

	    T item = parent2Rep.get(idx);
	    idx = parent1Rep.indexOf(item);

	    while (idx != indices.get(0)) {
		// add that index to the cycle indices
		indices.add(idx);
		// get the item in the second parent at that index
		item = parent2Rep.get(idx);
		// get the index of that item in the first parent
		idx = parent1Rep.indexOf(item);
	    }

	    // for even cycles: swap the child elements on the indices found in this cycle
	    if (cycle++ % 2 != 0) {
		for (int i : indices) {
		    T tmp = child1Rep.get(i);
		    child1Rep.set(i, child2Rep.get(i));
		    child2Rep.set(i, tmp);
		}
	    }

	    visitedIndices.addAll(indices);
	    // find next starting index: last one + 1 until we find an unvisited index
	    idx = (indices.get(0) + 1) % length;
	    while (visitedIndices.contains(idx) && visitedIndices.size() &lt; length) {
		idx++;
		if (idx &gt;= length) {
		    idx = 0;
		}
	    }
	    indices.clear();
	}

	return new ChromosomePair(first.newFixedLengthChromosome(child1Rep),
		second.newFixedLengthChromosome(child2Rep));
    }

    /** If the start index shall be chosen randomly. */
    private final boolean randomStart;

}

