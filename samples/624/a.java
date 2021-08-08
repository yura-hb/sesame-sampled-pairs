import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

abstract class AbstractHeaderCheck extends AbstractFileSetCheck implements ExternalResourceHolder {
    /**
     * Set the header file to check against.
     * @param uri the uri of the header to load.
     * @throws CheckstyleException if fileName is empty.
     */
    public void setHeaderFile(URI uri) throws CheckstyleException {
	if (uri == null) {
	    throw new CheckstyleException(
		    "property 'headerFile' is missing or invalid in module " + getConfiguration().getName());
	}

	headerFile = uri;
    }

    /** The file that contains the header to check against. */
    private URI headerFile;

}

