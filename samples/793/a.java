import org.eclipse.jdt.internal.core.*;

class ASTParser {
    /**
     * Sets the working copy owner used when resolving bindings, where
     * &lt;code&gt;null&lt;/code&gt; means the primary owner. Defaults to the primary owner.
     *
     * @param owner the owner of working copies that take precedence over underlying
     *   compilation units, or &lt;code&gt;null&lt;/code&gt; if the primary owner should be used
     */
    public void setWorkingCopyOwner(WorkingCopyOwner owner) {
	if (owner == null) {
	    this.workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
	} else {
	    this.workingCopyOwner = owner;
	}
    }

    /**
     * Working copy owner. Defaults to primary owner.
     */
    private WorkingCopyOwner workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;

}

