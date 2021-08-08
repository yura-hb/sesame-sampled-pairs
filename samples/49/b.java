import java.util.Collections;
import java.util.List;

class CompilationResult {
    /**
     * @return the list of {@link SourceMapping}s
     */
    public List&lt;SourceMapping&gt; getSourceMappings() {
	if (sourceMapping.isEmpty()) {
	    return emptyList();
	}
	return unmodifiableList(sourceMapping);
    }

    private final List&lt;SourceMapping&gt; sourceMapping = new ArrayList&lt;&gt;();

}

