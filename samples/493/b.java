import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLNamespaceBinder;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner;

class DTDConfiguration extends BasicParserConfiguration implements XMLPullParserConfiguration {
    /**
     * Parses the specified input source.
     *
     * @param source The input source.
     *
     * @exception XNIException Throws exception on XNI error.
     * @exception java.io.IOException Throws exception on i/o error.
     */
    public void parse(XMLInputSource source) throws XNIException, IOException {

	if (fParseInProgress) {
	    // REVISIT - need to add new error message
	    throw new XNIException("FWK005 parse may not be called while parsing.");
	}
	fParseInProgress = true;

	try {
	    setInputSource(source);
	    parse(true);
	} catch (XNIException ex) {
	    if (PRINT_EXCEPTION_STACK_TRACE)
		ex.printStackTrace();
	    throw ex;
	} catch (IOException ex) {
	    if (PRINT_EXCEPTION_STACK_TRACE)
		ex.printStackTrace();
	    throw ex;
	} catch (RuntimeException ex) {
	    if (PRINT_EXCEPTION_STACK_TRACE)
		ex.printStackTrace();
	    throw ex;
	} catch (Exception ex) {
	    if (PRINT_EXCEPTION_STACK_TRACE)
		ex.printStackTrace();
	    throw new XNIException(ex);
	} finally {
	    fParseInProgress = false;
	    // close all streams opened by xerces
	    this.cleanup();
	}

    }

    /**
     * True if a parse is in progress. This state is needed because
     * some features/properties cannot be set while parsing (e.g.
     * validation and namespaces).
     */
    protected boolean fParseInProgress = false;
    /** Set to true and recompile to print exception stack trace. */
    protected static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
    /** Input Source */
    protected XMLInputSource fInputSource;
    /** Document scanner. */
    protected XMLDocumentScanner fScanner;
    /** Entity manager. */
    protected XMLEntityManager fEntityManager;
    protected ValidationManager fValidationManager;
    /** DTD Validator. */
    protected XMLDTDValidator fDTDValidator;
    /** Namespace binder. */
    protected XMLNamespaceBinder fNamespaceBinder;
    /** DTD scanner. */
    protected XMLDTDScanner fDTDScanner;
    /** Property identifier: DTD scanner. */
    protected static final String DTD_SCANNER = Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;
    /** DTD Processor . */
    protected XMLDTDProcessor fDTDProcessor;
    /** Property identifier: DTD loader. */
    protected static final String DTD_PROCESSOR = Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_PROCESSOR_PROPERTY;

    /**
     * Sets the input source for the document to parse.
     *
     * @param inputSource The document's input source.
     *
     * @exception XMLConfigurationException Thrown if there is a
     *                        configuration error when initializing the
     *                        parser.
     * @exception IOException Thrown on I/O error.
     *
     * @see #parse(boolean)
     */
    public void setInputSource(XMLInputSource inputSource) throws XMLConfigurationException, IOException {

	// REVISIT: this method used to reset all the components and
	//          construct the pipeline. Now reset() is called
	//          in parse (boolean) just before we parse the document
	//          Should this method still throw exceptions..?

	fInputSource = inputSource;

    }

    /**
     * Parses the document in a pull parsing fashion.
     *
     * @param complete True if the pull parser should parse the
     *                 remaining document completely.
     *
     * @return True if there is more document to parse.
     *
     * @exception XNIException Any XNI exception, possibly wrapping
     *                         another exception.
     * @exception IOException  An IO exception from the parser, possibly
     *                         from a byte stream or character stream
     *                         supplied by the parser.
     *
     * @see #setInputSource
     */
    public boolean parse(boolean complete) throws XNIException, IOException {
	//
	// reset and configure pipeline and set InputSource.
	if (fInputSource != null) {
	    try {
		// resets and sets the pipeline.
		reset();
		fScanner.setInputSource(fInputSource);
		fInputSource = null;
	    } catch (XNIException ex) {
		if (PRINT_EXCEPTION_STACK_TRACE)
		    ex.printStackTrace();
		throw ex;
	    } catch (IOException ex) {
		if (PRINT_EXCEPTION_STACK_TRACE)
		    ex.printStackTrace();
		throw ex;
	    } catch (RuntimeException ex) {
		if (PRINT_EXCEPTION_STACK_TRACE)
		    ex.printStackTrace();
		throw ex;
	    } catch (Exception ex) {
		if (PRINT_EXCEPTION_STACK_TRACE)
		    ex.printStackTrace();
		throw new XNIException(ex);
	    }
	}

	try {
	    return fScanner.scanDocument(complete);
	} catch (XNIException ex) {
	    if (PRINT_EXCEPTION_STACK_TRACE)
		ex.printStackTrace();
	    throw ex;
	} catch (IOException ex) {
	    if (PRINT_EXCEPTION_STACK_TRACE)
		ex.printStackTrace();
	    throw ex;
	} catch (RuntimeException ex) {
	    if (PRINT_EXCEPTION_STACK_TRACE)
		ex.printStackTrace();
	    throw ex;
	} catch (Exception ex) {
	    if (PRINT_EXCEPTION_STACK_TRACE)
		ex.printStackTrace();
	    throw new XNIException(ex);
	}

    }

    /**
     * If the application decides to terminate parsing before the xml document
     * is fully parsed, the application should call this method to free any
     * resource allocated during parsing. For example, close all opened streams.
     */
    public void cleanup() {
	fEntityManager.closeReaders();
    }

    /**
     * Reset all components before parsing.
     *
     * @throws XNIException Thrown if an error occurs during initialization.
     */
    protected void reset() throws XNIException {

	if (fValidationManager != null)
	    fValidationManager.reset();
	// configure the pipeline and initialize the components
	configurePipeline();
	super.reset();
    }

    /** Configures the pipeline. */
    protected void configurePipeline() {

	// REVISIT: This should be better designed. In other words, we
	//          need to figure out what is the best way for people to
	//          re-use *most* of the standard configuration but do
	//          things common things such as remove a component (e.g.
	//          the validator), insert a new component (e.g. XInclude),
	//          etc... -Ac

	// setup document pipeline
	if (fDTDValidator != null) {
	    fScanner.setDocumentHandler(fDTDValidator);
	    if (fFeatures.get(NAMESPACES) == Boolean.TRUE) {

		// filters
		fDTDValidator.setDocumentHandler(fNamespaceBinder);
		fDTDValidator.setDocumentSource(fScanner);
		fNamespaceBinder.setDocumentHandler(fDocumentHandler);
		fNamespaceBinder.setDocumentSource(fDTDValidator);
		fLastComponent = fNamespaceBinder;
	    } else {
		fDTDValidator.setDocumentHandler(fDocumentHandler);
		fDTDValidator.setDocumentSource(fScanner);
		fLastComponent = fDTDValidator;
	    }
	} else {
	    if (fFeatures.get(NAMESPACES) == Boolean.TRUE) {
		fScanner.setDocumentHandler(fNamespaceBinder);
		fNamespaceBinder.setDocumentHandler(fDocumentHandler);
		fNamespaceBinder.setDocumentSource(fScanner);
		fLastComponent = fNamespaceBinder;
	    } else {
		fScanner.setDocumentHandler(fDocumentHandler);
		fLastComponent = fScanner;
	    }
	}

	configureDTDPipeline();
    }

    protected void configureDTDPipeline() {

	// setup dtd pipeline
	if (fDTDScanner != null) {
	    fProperties.put(DTD_SCANNER, fDTDScanner);
	    if (fDTDProcessor != null) {
		fProperties.put(DTD_PROCESSOR, fDTDProcessor);
		fDTDScanner.setDTDHandler(fDTDProcessor);
		fDTDProcessor.setDTDSource(fDTDScanner);
		fDTDProcessor.setDTDHandler(fDTDHandler);
		if (fDTDHandler != null) {
		    fDTDHandler.setDTDSource(fDTDProcessor);
		}

		fDTDScanner.setDTDContentModelHandler(fDTDProcessor);
		fDTDProcessor.setDTDContentModelSource(fDTDScanner);
		fDTDProcessor.setDTDContentModelHandler(fDTDContentModelHandler);
		if (fDTDContentModelHandler != null) {
		    fDTDContentModelHandler.setDTDContentModelSource(fDTDProcessor);
		}
	    } else {
		fDTDScanner.setDTDHandler(fDTDHandler);
		if (fDTDHandler != null) {
		    fDTDHandler.setDTDSource(fDTDScanner);
		}
		fDTDScanner.setDTDContentModelHandler(fDTDContentModelHandler);
		if (fDTDContentModelHandler != null) {
		    fDTDContentModelHandler.setDTDContentModelSource(fDTDScanner);
		}
	    }
	}

    }

}

