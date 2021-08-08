class BoundType extends Enum&lt;BoundType&gt; {
    /** Returns the bound type corresponding to a boolean value for inclusivity. */
    static BoundType forBoolean(boolean inclusive) {
	return inclusive ? CLOSED : OPEN;
    }

}

