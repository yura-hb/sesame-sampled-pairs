import sun.tools.tree.*;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;

class BinaryMember extends MemberDefinition {
    /**
     * Get the value
     */
    public Node getValue(Environment env) {
	if (isMethod()) {
	    return null;
	}
	if (!isFinal()) {
	    return null;
	}
	if (getValue() != null) {
	    return (Expression) getValue();
	}
	byte data[] = getAttribute(idConstantValue);
	if (data == null) {
	    return null;
	}

	try {
	    BinaryConstantPool cpool = ((BinaryClass) getClassDefinition()).getConstants();
	    // JVM 4.7.3 ConstantValue.constantvalue_index
	    Object obj = cpool.getValue(new DataInputStream(new ByteArrayInputStream(data)).readUnsignedShort());
	    switch (getType().getTypeCode()) {
	    case TC_BOOLEAN:
		setValue(new BooleanExpression(0, ((Number) obj).intValue() != 0));
		break;
	    case TC_BYTE:
	    case TC_SHORT:
	    case TC_CHAR:
	    case TC_INT:
		setValue(new IntExpression(0, ((Number) obj).intValue()));
		break;
	    case TC_LONG:
		setValue(new LongExpression(0, ((Number) obj).longValue()));
		break;
	    case TC_FLOAT:
		setValue(new FloatExpression(0, ((Number) obj).floatValue()));
		break;
	    case TC_DOUBLE:
		setValue(new DoubleExpression(0, ((Number) obj).doubleValue()));
		break;
	    case TC_CLASS:
		setValue(new StringExpression(0, (String) cpool.getValue(((Number) obj).intValue())));
		break;
	    }
	    return (Expression) getValue();
	} catch (IOException e) {
	    throw new CompilerError(e);
	}
    }

    BinaryAttribute atts;

    /**
     * Get a field attribute
     */
    public byte[] getAttribute(Identifier name) {
	for (BinaryAttribute att = atts; att != null; att = att.next) {
	    if (att.name.equals(name)) {
		return att.data;
	    }
	}
	return null;
    }

}

