import com.google.common.collect.FluentIterable;

class ClassPath {
    /** Returns all top level classes loadable from the current class path. */
    public ImmutableSet&lt;ClassInfo&gt; getTopLevelClasses() {
	return FluentIterable.from(resources).filter(ClassInfo.class).filter(IS_TOP_LEVEL).toSet();
    }

    private final ImmutableSet&lt;ResourceInfo&gt; resources;
    private static final Predicate&lt;ClassInfo&gt; IS_TOP_LEVEL = new Predicate&lt;ClassInfo&gt;() {
	@Override
	public boolean apply(ClassInfo info) {
	    return info.className.indexOf('$') == -1;
	}
    };

}

