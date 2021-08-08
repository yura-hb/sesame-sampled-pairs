import com.sun.org.apache.xml.internal.security.utils.Constants;

class SignatureProperty extends SignatureElementProxy {
    /**
     * Sets the {@code target} attribute
     *
     * @param target the {@code target} attribute
     */
    public void setTarget(String target) {
	if (target != null) {
	    setLocalAttribute(Constants._ATT_TARGET, target);
	}
    }

}

