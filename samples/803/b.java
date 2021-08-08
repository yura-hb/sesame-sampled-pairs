abstract class ScriptObject implements PropertyAccess, Cloneable {
    /**
     * Check if this script object is an internal object that should not be visible to script code.
     * @return true if internal
     */
    public final boolean isInternal() {
	return (flags & IS_INTERNAL) != 0;
    }

    /** Object flags. */
    private int flags;
    /** Is this an internal object that should not be visible to scripts? */
    public static final int IS_INTERNAL = 1 &lt;&lt; 4;

}

