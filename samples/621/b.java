import java.util.List;

class ClassBuilder extends AbstractBuilder {
    class MethodBuilder extends MemberBuilder {
	/**
	 * Adds a parameter(s) to the method method builder.
	 * @param params a pair consisting of type and parameter name.
	 * @return this method builder.
	 */
	public MethodBuilder addParameters(Pair... params) {
	    this.params.addAll(List.of(params));
	    return this;
	}

	private final List&lt;Pair&gt; params;

    }

}

