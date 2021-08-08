import java.text.MessageFormat;

class DTDParser implements DTDConstants {
    /**
     * Report an error.
     */
    void error(String err, String arg1, String arg2, String arg3) {
	nerrors++;

	String msgParams[] = { arg1, arg2, arg3 };

	String str = getSubstProp("dtderr." + err, msgParams);
	if (str == null) {
	    str = err + "[" + arg1 + "," + arg2 + "," + arg3 + "]";
	}
	System.err.println("line " + in.ln + ", dtd " + dtd + ": " + str);
    }

    int nerrors = 0;
    DTDInputStream in;
    DTDBuilder dtd;

    private String getSubstProp(String propName, String args[]) {
	String prop = System.getProperty(propName);

	if (prop == null) {
	    return null;
	}

	return MessageFormat.format(prop, (Object[]) args);
    }

}

