import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

class BoundSet {
    /** Answer a flat representation of this BoundSet. */
    public TypeBound[] flatten() {
	int size = 0;
	Iterator&lt;ThreeSets&gt; outerIt = this.boundsPerVariable.values().iterator();
	while (outerIt.hasNext())
	    size += outerIt.next().size();
	TypeBound[] collected = new TypeBound[size];
	if (size == 0)
	    return collected;
	outerIt = this.boundsPerVariable.values().iterator();
	int idx = 0;
	while (outerIt.hasNext())
	    idx = outerIt.next().flattenInto(collected, idx);
	return collected;
    }

    HashMap&lt;InferenceVariable, ThreeSets&gt; boundsPerVariable = new HashMap&lt;&gt;();

    class ThreeSets {
	HashMap&lt;InferenceVariable, ThreeSets&gt; boundsPerVariable = new HashMap&lt;&gt;();

	/** Total number of type bounds in this container. */
	public int size() {
	    int size = 0;
	    if (this.superBounds != null)
		size += this.superBounds.size();
	    if (this.sameBounds != null)
		size += this.sameBounds.size();
	    if (this.subBounds != null)
		size += this.subBounds.size();
	    return size;
	}

	public int flattenInto(TypeBound[] collected, int idx) {
	    if (this.superBounds != null) {
		int len = this.superBounds.size();
		System.arraycopy(this.superBounds.toArray(), 0, collected, idx, len);
		idx += len;
	    }
	    if (this.sameBounds != null) {
		int len = this.sameBounds.size();
		System.arraycopy(this.sameBounds.toArray(), 0, collected, idx, len);
		idx += len;
	    }
	    if (this.subBounds != null) {
		int len = this.subBounds.size();
		System.arraycopy(this.subBounds.toArray(), 0, collected, idx, len);
		idx += len;
	    }
	    return idx;
	}

    }

}

