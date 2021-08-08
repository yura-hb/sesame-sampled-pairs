import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.jdi.InternalException;

abstract class ReferenceTypeImpl extends TypeImpl implements ReferenceType {
    /**
     * Returns a map of field values
     */
    public Map&lt;Field, Value&gt; getValues(List&lt;? extends Field&gt; theFields) {
	validateMirrors(theFields);

	int size = theFields.size();
	JDWP.ReferenceType.GetValues.Field[] queryFields = new JDWP.ReferenceType.GetValues.Field[size];

	for (int i = 0; i &lt; size; i++) {
	    FieldImpl field = (FieldImpl) theFields.get(i);

	    validateFieldAccess(field);

	    // Do more validation specific to ReferenceType field getting
	    if (!field.isStatic()) {
		throw new IllegalArgumentException("Attempt to use non-static field with ReferenceType");
	    }
	    queryFields[i] = new JDWP.ReferenceType.GetValues.Field(field.ref());
	}

	Map&lt;Field, Value&gt; map = new HashMap&lt;Field, Value&gt;(size);

	ValueImpl[] values;
	try {
	    values = JDWP.ReferenceType.GetValues.process(vm, this, queryFields).values;
	} catch (JDWPException exc) {
	    throw exc.toJDIException();
	}

	if (size != values.length) {
	    throw new InternalException("Wrong number of values returned from target VM");
	}
	for (int i = 0; i &lt; size; i++) {
	    FieldImpl field = (FieldImpl) theFields.get(i);
	    map.put(field, values[i]);
	}

	return map;
    }

    void validateFieldAccess(Field field) {
	/*
	 * Field must be in this object's class, a superclass, or
	 * implemented interface
	 */
	ReferenceTypeImpl declType = (ReferenceTypeImpl) field.declaringType();
	if (!declType.isAssignableFrom(this)) {
	    throw new IllegalArgumentException("Invalid field");
	}
    }

    boolean isAssignableFrom(ReferenceType type) {
	return ((ReferenceTypeImpl) type).isAssignableTo(this);
    }

    abstract boolean isAssignableTo(ReferenceType type);

}

