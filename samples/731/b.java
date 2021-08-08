import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

class Quantiles {
    class ScaleAndIndexes {
	/**
	* Computes the quantile values of the given dataset, performing the computation in-place.
	*
	* @param dataset the dataset to do the calculation on, which must be non-empty, and which will
	*     be arbitrarily reordered by this method call
	* @return an unmodifiable map of results: the keys will be the specified quantile indexes, and
	*     the values the corresponding quantile values
	*/
	public Map&lt;Integer, Double&gt; computeInPlace(double... dataset) {
	    checkArgument(dataset.length &gt; 0, "Cannot calculate quantiles of an empty dataset");
	    if (containsNaN(dataset)) {
		Map&lt;Integer, Double&gt; nanMap = new HashMap&lt;&gt;();
		for (int index : indexes) {
		    nanMap.put(index, NaN);
		}
		return unmodifiableMap(nanMap);
	    }

	    // Calculate the quotients and remainders in the integer division x = k * (N - 1) / q, i.e.
	    // index * (dataset.length - 1) / scale for each index in indexes. For each, if there is no
	    // remainder, we can just select the value whose index in the sorted dataset equals the
	    // quotient; if there is a remainder, we interpolate between that and the next value.

	    int[] quotients = new int[indexes.length];
	    int[] remainders = new int[indexes.length];
	    // The indexes to select. In the worst case, we'll need one each side of each quantile.
	    int[] requiredSelections = new int[indexes.length * 2];
	    int requiredSelectionsCount = 0;
	    for (int i = 0; i &lt; indexes.length; i++) {
		// Since index and (dataset.length - 1) are non-negative ints, their product can be
		// expressed as a long, without risk of overflow:
		long numerator = (long) indexes[i] * (dataset.length - 1);
		// Since scale is a positive int, index is in [0, scale], and (dataset.length - 1) is a
		// non-negative int, we can do long-arithmetic on index * (dataset.length - 1) / scale to
		// get a rounded ratio and a remainder which can be expressed as ints, without risk of
		// overflow:
		int quotient = (int) LongMath.divide(numerator, scale, RoundingMode.DOWN);
		int remainder = (int) (numerator - (long) quotient * scale);
		quotients[i] = quotient;
		remainders[i] = remainder;
		requiredSelections[requiredSelectionsCount] = quotient;
		requiredSelectionsCount++;
		if (remainder != 0) {
		    requiredSelections[requiredSelectionsCount] = quotient + 1;
		    requiredSelectionsCount++;
		}
	    }
	    sort(requiredSelections, 0, requiredSelectionsCount);
	    selectAllInPlace(requiredSelections, 0, requiredSelectionsCount - 1, dataset, 0, dataset.length - 1);
	    Map&lt;Integer, Double&gt; ret = new HashMap&lt;&gt;();
	    for (int i = 0; i &lt; indexes.length; i++) {
		int quotient = quotients[i];
		int remainder = remainders[i];
		if (remainder == 0) {
		    ret.put(indexes[i], dataset[quotient]);
		} else {
		    ret.put(indexes[i], interpolate(dataset[quotient], dataset[quotient + 1], remainder, scale));
		}
	    }
	    return unmodifiableMap(ret);
	}

	private final int[] indexes;
	private final int scale;

    }

    /** Returns whether any of the values in {@code dataset} are {@code NaN}. */
    private static boolean containsNaN(double... dataset) {
	for (double value : dataset) {
	    if (Double.isNaN(value)) {
		return true;
	    }
	}
	return false;
    }

    /**
    * Performs an in-place selection, like {@link #selectInPlace}, to select all the indexes {@code
    * allRequired[i]} for {@code i} in the range [{@code requiredFrom}, {@code requiredTo}]. These
    * indexes must be sorted in the array and must all be in the range [{@code from}, {@code to}].
    */
    private static void selectAllInPlace(int[] allRequired, int requiredFrom, int requiredTo, double[] array, int from,
	    int to) {
	// Choose the first selection to do...
	int requiredChosen = chooseNextSelection(allRequired, requiredFrom, requiredTo, from, to);
	int required = allRequired[requiredChosen];

	// ...do the first selection...
	selectInPlace(required, array, from, to);

	// ...then recursively perform the selections in the range below...
	int requiredBelow = requiredChosen - 1;
	while (requiredBelow &gt;= requiredFrom && allRequired[requiredBelow] == required) {
	    requiredBelow--; // skip duplicates of required in the range below
	}
	if (requiredBelow &gt;= requiredFrom) {
	    selectAllInPlace(allRequired, requiredFrom, requiredBelow, array, from, required - 1);
	}

	// ...and then recursively perform the selections in the range above.
	int requiredAbove = requiredChosen + 1;
	while (requiredAbove &lt;= requiredTo && allRequired[requiredAbove] == required) {
	    requiredAbove++; // skip duplicates of required in the range above
	}
	if (requiredAbove &lt;= requiredTo) {
	    selectAllInPlace(allRequired, requiredAbove, requiredTo, array, required + 1, to);
	}
    }

    /**
    * Returns a value a fraction {@code (remainder / scale)} of the way between {@code lower} and
    * {@code upper}. Assumes that {@code lower &lt;= upper}. Correctly handles infinities (but not
    * {@code NaN}).
    */
    private static double interpolate(double lower, double upper, double remainder, double scale) {
	if (lower == NEGATIVE_INFINITY) {
	    if (upper == POSITIVE_INFINITY) {
		// Return NaN when lower == NEGATIVE_INFINITY and upper == POSITIVE_INFINITY:
		return NaN;
	    }
	    // Return NEGATIVE_INFINITY when NEGATIVE_INFINITY == lower &lt;= upper &lt; POSITIVE_INFINITY:
	    return NEGATIVE_INFINITY;
	}
	if (upper == POSITIVE_INFINITY) {
	    // Return POSITIVE_INFINITY when NEGATIVE_INFINITY &lt; lower &lt;= upper == POSITIVE_INFINITY:
	    return POSITIVE_INFINITY;
	}
	return lower + (upper - lower) * remainder / scale;
    }

    /**
    * Chooses the next selection to do from the required selections. It is required that the array
    * {@code allRequired} is sorted and that {@code allRequired[i]} are in the range [{@code from},
    * {@code to}] for all {@code i} in the range [{@code requiredFrom}, {@code requiredTo}]. The
    * value returned by this method is the {@code i} in that range such that {@code allRequired[i]}
    * is as close as possible to the center of the range [{@code from}, {@code to}]. Choosing the
    * value closest to the center of the range first is the most efficient strategy because it
    * minimizes the size of the subranges from which the remaining selections must be done.
    */
    private static int chooseNextSelection(int[] allRequired, int requiredFrom, int requiredTo, int from, int to) {
	if (requiredFrom == requiredTo) {
	    return requiredFrom; // only one thing to choose, so choose it
	}

	// Find the center and round down. The true center is either centerFloor or halfway between
	// centerFloor and centerFloor + 1.
	int centerFloor = (from + to) &gt;&gt;&gt; 1;

	// Do a binary search until we're down to the range of two which encloses centerFloor (unless
	// all values are lower or higher than centerFloor, in which case we find the two highest or
	// lowest respectively). If centerFloor is in allRequired, we will definitely find it. If not,
	// but centerFloor + 1 is, we'll definitely find that. The closest value to the true (unrounded)
	// center will be at either low or high.
	int low = requiredFrom;
	int high = requiredTo;
	while (high &gt; low + 1) {
	    int mid = (low + high) &gt;&gt;&gt; 1;
	    if (allRequired[mid] &gt; centerFloor) {
		high = mid;
	    } else if (allRequired[mid] &lt; centerFloor) {
		low = mid;
	    } else {
		return mid; // allRequired[mid] = centerFloor, so we can't get closer than that
	    }
	}

	// Now pick the closest of the two candidates. Note that there is no rounding here.
	if (from + to - allRequired[low] - allRequired[high] &gt; 0) {
	    return high;
	} else {
	    return low;
	}
    }

    /**
    * Performs an in-place selection to find the element which would appear at a given index in a
    * dataset if it were sorted. The following preconditions should hold:
    *
    * &lt;ul&gt;
    *   &lt;li&gt;{@code required}, {@code from}, and {@code to} should all be indexes into {@code array};
    *   &lt;li&gt;{@code required} should be in the range [{@code from}, {@code to}];
    *   &lt;li&gt;all the values with indexes in the range [0, {@code from}) should be less than or equal
    *       to all the values with indexes in the range [{@code from}, {@code to}];
    *   &lt;li&gt;all the values with indexes in the range ({@code to}, {@code array.length - 1}] should be
    *       greater than or equal to all the values with indexes in the range [{@code from}, {@code
    *       to}].
    * &lt;/ul&gt;
    *
    * This method will reorder the values with indexes in the range [{@code from}, {@code to}] such
    * that all the values with indexes in the range [{@code from}, {@code required}) are less than or
    * equal to the value with index {@code required}, and all the values with indexes in the range
    * ({@code required}, {@code to}] are greater than or equal to that value. Therefore, the value at
    * {@code required} is the value which would appear at that index in the sorted dataset.
    */
    private static void selectInPlace(int required, double[] array, int from, int to) {
	// If we are looking for the least element in the range, we can just do a linear search for it.
	// (We will hit this whenever we are doing quantile interpolation: our first selection finds
	// the lower value, our second one finds the upper value by looking for the next least element.)
	if (required == from) {
	    int min = from;
	    for (int index = from + 1; index &lt;= to; index++) {
		if (array[min] &gt; array[index]) {
		    min = index;
		}
	    }
	    if (min != from) {
		swap(array, min, from);
	    }
	    return;
	}

	// Let's play quickselect! We'll repeatedly partition the range [from, to] containing the
	// required element, as long as it has more than one element.
	while (to &gt; from) {
	    int partitionPoint = partition(array, from, to);
	    if (partitionPoint &gt;= required) {
		to = partitionPoint - 1;
	    }
	    if (partitionPoint &lt;= required) {
		from = partitionPoint + 1;
	    }
	}
    }

    /** Swaps the values at {@code i} and {@code j} in {@code array}. */
    private static void swap(double[] array, int i, int j) {
	double temp = array[i];
	array[i] = array[j];
	array[j] = temp;
    }

    /**
    * Performs a partition operation on the slice of {@code array} with elements in the range [{@code
    * from}, {@code to}]. Uses the median of {@code from}, {@code to}, and the midpoint between them
    * as a pivot. Returns the index which the slice is partitioned around, i.e. if it returns {@code
    * ret} then we know that the values with indexes in [{@code from}, {@code ret}) are less than or
    * equal to the value at {@code ret} and the values with indexes in ({@code ret}, {@code to}] are
    * greater than or equal to that.
    */
    private static int partition(double[] array, int from, int to) {
	// Select a pivot, and move it to the start of the slice i.e. to index from.
	movePivotToStartOfSlice(array, from, to);
	double pivot = array[from];

	// Move all elements with indexes in (from, to] which are greater than the pivot to the end of
	// the array. Keep track of where those elements begin.
	int partitionPoint = to;
	for (int i = to; i &gt; from; i--) {
	    if (array[i] &gt; pivot) {
		swap(array, partitionPoint, i);
		partitionPoint--;
	    }
	}

	// We now know that all elements with indexes in (from, partitionPoint] are less than or equal
	// to the pivot at from, and all elements with indexes in (partitionPoint, to] are greater than
	// it. We swap the pivot into partitionPoint and we know the array is partitioned around that.
	swap(array, from, partitionPoint);
	return partitionPoint;
    }

    /**
    * Selects the pivot to use, namely the median of the values at {@code from}, {@code to}, and
    * halfway between the two (rounded down), from {@code array}, and ensure (by swapping elements if
    * necessary) that that pivot value appears at the start of the slice i.e. at {@code from}.
    * Expects that {@code from} is strictly less than {@code to}.
    */
    private static void movePivotToStartOfSlice(double[] array, int from, int to) {
	int mid = (from + to) &gt;&gt;&gt; 1;
	// We want to make a swap such that either array[to] &lt;= array[from] &lt;= array[mid], or
	// array[mid] &lt;= array[from] &lt;= array[to]. We know that from &lt; to, so we know mid &lt; to
	// (although it's possible that mid == from, if to == from + 1). Note that the postcondition
	// would be impossible to fulfil if mid == to unless we also have array[from] == array[to].
	boolean toLessThanMid = (array[to] &lt; array[mid]);
	boolean midLessThanFrom = (array[mid] &lt; array[from]);
	boolean toLessThanFrom = (array[to] &lt; array[from]);
	if (toLessThanMid == midLessThanFrom) {
	    // Either array[to] &lt; array[mid] &lt; array[from] or array[from] &lt;= array[mid] &lt;= array[to].
	    swap(array, mid, from);
	} else if (toLessThanMid != toLessThanFrom) {
	    // Either array[from] &lt;= array[to] &lt; array[mid] or array[mid] &lt;= array[to] &lt; array[from].
	    swap(array, from, to);
	}
	// The postcondition now holds. So the median, our chosen pivot, is at from.
    }

}

