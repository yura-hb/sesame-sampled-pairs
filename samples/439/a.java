abstract class ReferenceBinding extends TypeBinding {
    /**
    * Answer true if the receiver has private visibility
    */
    public final boolean isPrivate() {
	return (this.modifiers & ClassFileConstants.AccPrivate) != 0;
    }

    public int modifiers;

}

