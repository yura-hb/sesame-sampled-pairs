import org.apache.commons.math4.RealFieldElement;

class MathUtils {
    /** Find the maximum of two field elements.
      * @param &lt;T&gt; the type of the field elements
      * @param e1 first element
      * @param e2 second element
      * @return max(a1, e2)
      * @since 3.6
      */
    public static &lt;T extends RealFieldElement&lt;T&gt;&gt; T max(final T e1, final T e2) {
	return e1.subtract(e2).getReal() &gt;= 0 ? e1 : e2;
    }

}

