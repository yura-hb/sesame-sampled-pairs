import com.sun.org.apache.xerces.internal.dom.DOMErrorImpl;
import com.sun.org.apache.xerces.internal.util.DOMErrorHandlerWrapper;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import java.io.StringReader;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;

class DOMParserImpl extends AbstractDOMParser implements LSParser, DOMConfiguration {
    /**
     * NON-DOM: convert LSInput to XNIInputSource
     *
     * @param is
     * @return
     */
    XMLInputSource dom2xmlInputSource(LSInput is) {
	// need to wrap the LSInput with an XMLInputSource
	XMLInputSource xis = null;
	// check whether there is a Reader
	// according to DOM, we need to treat such reader as "UTF-16".
	if (is.getCharacterStream() != null) {
	    xis = new XMLInputSource(is.getPublicId(), is.getSystemId(), is.getBaseURI(), is.getCharacterStream(),
		    "UTF-16");
	}
	// check whether there is an InputStream
	else if (is.getByteStream() != null) {
	    xis = new XMLInputSource(is.getPublicId(), is.getSystemId(), is.getBaseURI(), is.getByteStream(),
		    is.getEncoding());
	}
	// if there is a string data, use a StringReader
	// according to DOM, we need to treat such data as "UTF-16".
	else if (is.getStringData() != null && is.getStringData().length() &gt; 0) {
	    xis = new XMLInputSource(is.getPublicId(), is.getSystemId(), is.getBaseURI(),
		    new StringReader(is.getStringData()), "UTF-16");
	}
	// otherwise, just use the public/system/base Ids
	else if ((is.getSystemId() != null && is.getSystemId().length() &gt; 0)
		|| (is.getPublicId() != null && is.getPublicId().length() &gt; 0)) {
	    xis = new XMLInputSource(is.getPublicId(), is.getSystemId(), is.getBaseURI(), false);
	} else {
	    // all inputs are null
	    if (fErrorHandler != null) {
		DOMErrorImpl error = new DOMErrorImpl();
		error.fType = "no-input-specified";
		error.fMessage = "no-input-specified";
		error.fSeverity = DOMError.SEVERITY_FATAL_ERROR;
		fErrorHandler.getErrorHandler().handleError(error);
	    }
	    throw new LSException(LSException.PARSE_ERR, "no-input-specified");
	}
	return xis;
    }

}

