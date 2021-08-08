import org.datavec.api.writable.WritableType;

class ColumnType extends Enum&lt;ColumnType&gt; {
    /**
     * @return The type of writable for this column
     */
    public WritableType getWritableType() {
	switch (this) {
	case String:
	    return WritableType.Text;
	case Integer:
	    return WritableType.Int;
	case Long:
	    return WritableType.Long;
	case Double:
	    return WritableType.Double;
	case Float:
	    return WritableType.Float;
	case Categorical:
	    return WritableType.Text;
	case Time:
	    return WritableType.Long;
	case Bytes:
	    return WritableType.Byte;
	case Boolean:
	    return WritableType.Boolean;
	case NDArray:
	    return WritableType.Image;
	default:
	    throw new IllegalStateException("Unknown writable type for column type: " + this);
	}
    }

}

