import com.sun.org.apache.xml.internal.security.utils.Constants;

class SignatureProperty extends SignatureElementProxy {
    /**
     *   Sets the {@code id} attribute
     *
     *   @param id the {@code id} attribute
     */
    public void setId(String id) {
	if (id != null) {
	    setLocalIdAttribute(Constants._ATT_ID, id);
	}
    }

}

