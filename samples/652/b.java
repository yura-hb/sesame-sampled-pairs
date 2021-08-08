import org.eclipse.jdt.internal.compiler.lookup.*;

abstract class Engine implements ITypeRequestor {
    /**
     * Add an additional binary type
     */
    @Override
    public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
	this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
    }

    public LookupEnvironment lookupEnvironment;

}

