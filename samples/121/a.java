import static com.google.common.collect.BoundType.CLOSED;
import static com.google.common.collect.BoundType.OPEN;
import java.util.Comparator;

class GeneralRange&lt;T&gt; implements Serializable {
    /**
    * Returns the intersection of the two ranges, or an empty range if their intersection is empty.
    */
    GeneralRange&lt;T&gt; intersect(GeneralRange&lt;T&gt; other) {
	checkNotNull(other);
	checkArgument(comparator.equals(other.comparator));

	boolean hasLowBound = this.hasLowerBound;
	@Nullable
	T lowEnd = getLowerEndpoint();
	BoundType lowType = getLowerBoundType();
	if (!hasLowerBound()) {
	    hasLowBound = other.hasLowerBound;
	    lowEnd = other.getLowerEndpoint();
	    lowType = other.getLowerBoundType();
	} else if (other.hasLowerBound()) {
	    int cmp = comparator.compare(getLowerEndpoint(), other.getLowerEndpoint());
	    if (cmp &lt; 0 || (cmp == 0 && other.getLowerBoundType() == OPEN)) {
		lowEnd = other.getLowerEndpoint();
		lowType = other.getLowerBoundType();
	    }
	}

	boolean hasUpBound = this.hasUpperBound;
	@Nullable
	T upEnd = getUpperEndpoint();
	BoundType upType = getUpperBoundType();
	if (!hasUpperBound()) {
	    hasUpBound = other.hasUpperBound;
	    upEnd = other.getUpperEndpoint();
	    upType = other.getUpperBoundType();
	} else if (other.hasUpperBound()) {
	    int cmp = comparator.compare(getUpperEndpoint(), other.getUpperEndpoint());
	    if (cmp &gt; 0 || (cmp == 0 && other.getUpperBoundType() == OPEN)) {
		upEnd = other.getUpperEndpoint();
		upType = other.getUpperBoundType();
	    }
	}

	if (hasLowBound && hasUpBound) {
	    int cmp = comparator.compare(lowEnd, upEnd);
	    if (cmp &gt; 0 || (cmp == 0 && lowType == OPEN && upType == OPEN)) {
		// force allowed empty range
		lowEnd = upEnd;
		lowType = OPEN;
		upType = CLOSED;
	    }
	}

	return new GeneralRange&lt;T&gt;(comparator, hasLowBound, lowEnd, lowType, hasUpBound, upEnd, upType);
    }

    private final Comparator&lt;? super T&gt; comparator;
    private final boolean hasLowerBound;
    private final boolean hasUpperBound;
    private final @Nullable T lowerEndpoint;
    private final BoundType lowerBoundType;
    private final @Nullable T upperEndpoint;
    private final BoundType upperBoundType;

    T getLowerEndpoint() {
	return lowerEndpoint;
    }

    BoundType getLowerBoundType() {
	return lowerBoundType;
    }

    boolean hasLowerBound() {
	return hasLowerBound;
    }

    T getUpperEndpoint() {
	return upperEndpoint;
    }

    BoundType getUpperBoundType() {
	return upperBoundType;
    }

    boolean hasUpperBound() {
	return hasUpperBound;
    }

    private GeneralRange(Comparator&lt;? super T&gt; comparator, boolean hasLowerBound, @Nullable T lowerEndpoint,
	    BoundType lowerBoundType, boolean hasUpperBound, @Nullable T upperEndpoint, BoundType upperBoundType) {
	this.comparator = checkNotNull(comparator);
	this.hasLowerBound = hasLowerBound;
	this.hasUpperBound = hasUpperBound;
	this.lowerEndpoint = lowerEndpoint;
	this.lowerBoundType = checkNotNull(lowerBoundType);
	this.upperEndpoint = upperEndpoint;
	this.upperBoundType = checkNotNull(upperBoundType);

	if (hasLowerBound) {
	    comparator.compare(lowerEndpoint, lowerEndpoint);
	}
	if (hasUpperBound) {
	    comparator.compare(upperEndpoint, upperEndpoint);
	}
	if (hasLowerBound && hasUpperBound) {
	    int cmp = comparator.compare(lowerEndpoint, upperEndpoint);
	    // be consistent with Range
	    checkArgument(cmp &lt;= 0, "lowerEndpoint (%s) &gt; upperEndpoint (%s)", lowerEndpoint, upperEndpoint);
	    if (cmp == 0) {
		checkArgument(lowerBoundType != OPEN | upperBoundType != OPEN);
	    }
	}
    }

}

