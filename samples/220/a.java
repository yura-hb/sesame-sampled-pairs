import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;

class CompilationUnitStructureRequestor extends ReferenceInfoAdapter implements ISourceElementRequestor {
    /**
    * Resolves duplicate handles by incrementing the occurrence count
    * of the handle being created.
    */
    protected void resolveDuplicates(SourceRefElement handle) {
	int occurenceCount = this.occurenceCounts.get(handle);
	if (occurenceCount == -1)
	    this.occurenceCounts.put(handle, 1);
	else {
	    this.occurenceCounts.put(handle, ++occurenceCount);
	    handle.occurrenceCount = occurenceCount;
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=342393
	// For anonymous source types, the occurrence count should be in the context
	// of the enclosing type.
	if (handle instanceof SourceType && ((SourceType) handle).isAnonymous()) {
	    Object key = handle.getParent().getAncestor(IJavaElement.TYPE);
	    occurenceCount = this.localOccurrenceCounts.get(key);
	    if (occurenceCount == -1)
		this.localOccurrenceCounts.put(key, 1);
	    else {
		this.localOccurrenceCounts.put(key, ++occurenceCount);
		((SourceType) handle).localOccurrenceCount = occurenceCount;
	    }
	}
    }

    private HashtableOfObjectToInt occurenceCounts;
    private HashtableOfObjectToInt localOccurrenceCounts;

}

