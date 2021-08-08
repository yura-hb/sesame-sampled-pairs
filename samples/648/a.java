import java.util.Arrays;

class ParameterNameCheck extends AbstractNameCheck {
    /**
     * Setter to access modifiers of methods where parameters are checked.
     * @param accessModifiers access modifiers of methods which should be checked.
     */
    public void setAccessModifiers(AccessModifier... accessModifiers) {
	this.accessModifiers = Arrays.copyOf(accessModifiers, accessModifiers.length);
    }

    /** Access modifiers of methods where parameters are checked. */
    private AccessModifier[] accessModifiers = { AccessModifier.PUBLIC, AccessModifier.PROTECTED,
	    AccessModifier.PACKAGE, AccessModifier.PRIVATE, };

}

