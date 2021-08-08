class FieldEquationsMapper&lt;T&gt; implements Serializable {
    /** Return the dimension of the complete set of equations.
     * &lt;p&gt;
     * The complete set of equations correspond to the primary set plus all secondary sets.
     * &lt;/p&gt;
     * @return dimension of the complete set of equations
     */
    public int getTotalDimension() {
	return start[start.length - 1];
    }

    /** Start indices of the components. */
    private final int[] start;

}

