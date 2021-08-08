import org.eclipse.jdt.core.compiler.CharOperation;

abstract class ReferenceBinding extends TypeBinding {
    /**
    * Answer the receiver's constant pool name.
    *
    * NOTE: This method should only be used during/after code gen.
    */
    @Override
    public char[] constantPoolName() /* java/lang/Object */ {
	if (this.constantPoolName != null)
	    return this.constantPoolName;
	return this.constantPoolName = CharOperation.concatWith(this.compoundName, '/');
    }

    char[] constantPoolName;
    public char[][] compoundName;

}

