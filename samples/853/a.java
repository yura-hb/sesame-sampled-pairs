import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

abstract class Member extends SourceRefElement implements IMember {
    /**
    * Converts a field constant from the compiler's representation
    * to the Java Model constant representation (Number or String).
    */
    protected static Object convertConstant(Constant constant) {
	if (constant == null)
	    return null;
	if (constant == Constant.NotAConstant) {
	    return null;
	}
	switch (constant.typeID()) {
	case TypeIds.T_boolean:
	    return constant.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
	case TypeIds.T_byte:
	    return Byte.valueOf(constant.byteValue());
	case TypeIds.T_char:
	    return Character.valueOf(constant.charValue());
	case TypeIds.T_double:
	    return new Double(constant.doubleValue());
	case TypeIds.T_float:
	    return new Float(constant.floatValue());
	case TypeIds.T_int:
	    return Integer.valueOf(constant.intValue());
	case TypeIds.T_long:
	    return Long.valueOf(constant.longValue());
	case TypeIds.T_short:
	    return Short.valueOf(constant.shortValue());
	case TypeIds.T_JavaLangString:
	    return constant.stringValue();
	default:
	    return null;
	}
    }

}

