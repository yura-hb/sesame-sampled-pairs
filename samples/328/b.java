import java.util.Map;

class Context {
    /**
     * Create a new builtin switchpoint and return it
     * @param name key name
     * @return new builtin switchpoint
     */
    public SwitchPoint newBuiltinSwitchPoint(final String name) {
	assert builtinSwitchPoints.get(name) == null;
	final SwitchPoint sp = new BuiltinSwitchPoint();
	builtinSwitchPoints.put(name, sp);
	return sp;
    }

    /**
     * Keeps track of which builtin prototypes and properties have been relinked
     * Currently we are conservative and associate the name of a builtin class with all
     * its properties, so it's enough to invalidate a property to break all assumptions
     * about a prototype. This can be changed to a more fine grained approach, but no one
     * ever needs this, given the very rare occurrence of swapping out only parts of
     * a builtin v.s. the entire builtin object
     */
    private final Map&lt;String, SwitchPoint&gt; builtinSwitchPoints = new HashMap&lt;&gt;();

}

