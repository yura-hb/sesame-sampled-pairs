class Joiner {
    /**
    * Returns a joiner with the same behavior as this one, except automatically substituting {@code
    * nullText} for any provided null elements.
    */
    public Joiner useForNull(final String nullText) {
	checkNotNull(nullText);
	return new Joiner(this) {
	    @Override
	    CharSequence toString(@Nullable Object part) {
		return (part == null) ? nullText : Joiner.this.toString(part);
	    }

	    @Override
	    public Joiner useForNull(String nullText) {
		throw new UnsupportedOperationException("already specified useForNull");
	    }

	    @Override
	    public Joiner skipNulls() {
		throw new UnsupportedOperationException("already specified useForNull");
	    }
	};
    }

    CharSequence toString(Object part) {
	checkNotNull(part); // checkNotNull for GWT (do not optimize).
	return (part instanceof CharSequence) ? (CharSequence) part : part.toString();
    }

}

