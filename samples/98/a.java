class FieldVector3D&lt;T&gt; implements Serializable {
    /** Get the square of the norm for the vector.
     * @return square of the Euclidean norm for the vector
     */
    public T getNormSq() {
	// there are no cancellation problems here, so we use the straightforward formula
	return x.multiply(x).add(y.multiply(y)).add(z.multiply(z));
    }

    /** Abscissa. */
    private final T x;
    /** Ordinate. */
    private final T y;
    /** Height. */
    private final T z;

}

