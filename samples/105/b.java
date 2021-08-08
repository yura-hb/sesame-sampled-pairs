class MathUtils {
    /**
     * Tests if a is smaller than b.
     *
     * @param a a double
     * @param b a double
     */
    public static /*@pure@*/ boolean sm(double a, double b) {

	return (b - a &gt; SMALL);
    }

    /** The small deviation allowed in double comparisons. */
    public static double SMALL = 1e-6;

}

