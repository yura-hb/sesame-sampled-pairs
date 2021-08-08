import com.google.common.primitives.Primitives;
import java.util.Set;

abstract class TypeToken&lt;T&gt; extends TypeCapture&lt;T&gt; implements Serializable {
    /**
    * Returns the corresponding primitive type if this is a wrapper type; otherwise returns {@code
    * this} itself. Idempotent.
    *
    * @since 15.0
    */
    public final TypeToken&lt;T&gt; unwrap() {
	if (isWrapper()) {
	    @SuppressWarnings("unchecked") // this is a wrapper class
	    Class&lt;T&gt; type = (Class&lt;T&gt;) runtimeType;
	    return of(Primitives.unwrap(type));
	}
	return this;
    }

    private final Type runtimeType;

    private boolean isWrapper() {
	return Primitives.allWrapperTypes().contains(runtimeType);
    }

    /** Returns an instance of type token that wraps {@code type}. */
    public static &lt;T&gt; TypeToken&lt;T&gt; of(Class&lt;T&gt; type) {
	return new SimpleTypeToken&lt;T&gt;(type);
    }

    private TypeToken(Type type) {
	this.runtimeType = checkNotNull(type);
    }

    class SimpleTypeToken&lt;T&gt; extends TypeToken&lt;T&gt; {
	private final Type runtimeType;

	SimpleTypeToken(Type type) {
	    super(type);
	}

    }

}

