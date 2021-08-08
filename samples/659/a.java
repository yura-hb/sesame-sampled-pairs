abstract class AbstractParameterizable implements Parameterizable {
    /** Check if a parameter is supported and throw an IllegalArgumentException if not.
     * @param name name of the parameter to check
     * @exception UnknownParameterException if the parameter is not supported
     * @see #isSupported(String)
     */
    public void complainIfNotSupported(final String name) throws UnknownParameterException {
	if (!isSupported(name)) {
	    throw new UnknownParameterException(name);
	}
    }

    /** List of the parameters names. */
    private final Collection&lt;String&gt; parametersNames;

    /** {@inheritDoc} */
    @Override
    public boolean isSupported(final String name) {
	for (final String supportedName : parametersNames) {
	    if (supportedName.equals(name)) {
		return true;
	    }
	}
	return false;
    }

}

