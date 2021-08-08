import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ShouldNotImplement;
import org.eclipse.jdt.internal.compiler.util.Messages;

abstract class Constant implements TypeIds, OperatorIds {
    /**
     * Returns true if both constants have the same type and the same actual value
     * @param otherConstant
     */
    public boolean hasSameValue(Constant otherConstant) {
	if (this == otherConstant)
	    return true;
	int typeID;
	if ((typeID = typeID()) != otherConstant.typeID())
	    return false;
	switch (typeID) {
	case TypeIds.T_boolean:
	    return booleanValue() == otherConstant.booleanValue();
	case TypeIds.T_byte:
	    return byteValue() == otherConstant.byteValue();
	case TypeIds.T_char:
	    return charValue() == otherConstant.charValue();
	case TypeIds.T_double:
	    return doubleValue() == otherConstant.doubleValue();
	case TypeIds.T_float:
	    return floatValue() == otherConstant.floatValue();
	case TypeIds.T_int:
	    return intValue() == otherConstant.intValue();
	case TypeIds.T_short:
	    return shortValue() == otherConstant.shortValue();
	case TypeIds.T_long:
	    return longValue() == otherConstant.longValue();
	case TypeIds.T_JavaLangString:
	    String value = stringValue();
	    return value == null ? otherConstant.stringValue() == null : value.equals(otherConstant.stringValue());
	}
	return false;
    }

    public abstract int typeID();

    public boolean booleanValue() {
	throw new ShouldNotImplement(
		Messages.bind(Messages.constant_cannotCastedInto, new String[] { typeName(), "boolean" })); //$NON-NLS-1$
    }

    public byte byteValue() {
	throw new ShouldNotImplement(
		Messages.bind(Messages.constant_cannotCastedInto, new String[] { typeName(), "byte" })); //$NON-NLS-1$
    }

    public char charValue() {
	throw new ShouldNotImplement(
		Messages.bind(Messages.constant_cannotCastedInto, new String[] { typeName(), "char" })); //$NON-NLS-1$
    }

    public double doubleValue() {
	throw new ShouldNotImplement(
		Messages.bind(Messages.constant_cannotCastedInto, new String[] { typeName(), "double" })); //$NON-NLS-1$
    }

    public float floatValue() {
	throw new ShouldNotImplement(
		Messages.bind(Messages.constant_cannotCastedInto, new String[] { typeName(), "float" })); //$NON-NLS-1$
    }

    public int intValue() {
	throw new ShouldNotImplement(
		Messages.bind(Messages.constant_cannotCastedInto, new String[] { typeName(), "int" })); //$NON-NLS-1$
    }

    public short shortValue() {
	throw new ShouldNotImplement(
		Messages.bind(Messages.constant_cannotConvertedTo, new String[] { typeName(), "short" })); //$NON-NLS-1$
    }

    public long longValue() {
	throw new ShouldNotImplement(
		Messages.bind(Messages.constant_cannotCastedInto, new String[] { typeName(), "long" })); //$NON-NLS-1$
    }

    public String stringValue() {
	throw new ShouldNotImplement(
		Messages.bind(Messages.constant_cannotConvertedTo, new String[] { typeName(), "String" })); //$NON-NLS-1$
    }

    public String typeName() {
	switch (typeID()) {
	case T_int:
	    return "int"; //$NON-NLS-1$
	case T_byte:
	    return "byte"; //$NON-NLS-1$
	case T_short:
	    return "short"; //$NON-NLS-1$
	case T_char:
	    return "char"; //$NON-NLS-1$
	case T_float:
	    return "float"; //$NON-NLS-1$
	case T_double:
	    return "double"; //$NON-NLS-1$
	case T_boolean:
	    return "boolean"; //$NON-NLS-1$
	case T_long:
	    return "long";//$NON-NLS-1$
	case T_JavaLangString:
	    return "java.lang.String"; //$NON-NLS-1$
	default:
	    return "unknown"; //$NON-NLS-1$
	}
    }

}

