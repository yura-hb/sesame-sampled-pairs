import java.util.function.BiPredicate;
import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.code.Symbol.*;
import static com.sun.tools.javac.code.Type.*;

class Types {
    /**
     * Form the union of two closures
     */
    public List&lt;Type&gt; union(List&lt;Type&gt; cl1, List&lt;Type&gt; cl2, BiPredicate&lt;Type, Type&gt; shouldSkip) {
	if (cl1.isEmpty()) {
	    return cl2;
	} else if (cl2.isEmpty()) {
	    return cl1;
	} else if (shouldSkip.test(cl1.head, cl2.head)) {
	    return union(cl1.tail, cl2.tail, shouldSkip).prepend(cl1.head);
	} else if (cl1.head.tsym.precedes(cl2.head.tsym, this)) {
	    return union(cl1.tail, cl2, shouldSkip).prepend(cl1.head);
	} else if (cl2.head.tsym.precedes(cl1.head.tsym, this)) {
	    return union(cl1, cl2.tail, shouldSkip).prepend(cl2.head);
	} else {
	    // unrelated types
	    return union(cl1.tail, cl2, shouldSkip).prepend(cl1.head);
	}
    }

}

