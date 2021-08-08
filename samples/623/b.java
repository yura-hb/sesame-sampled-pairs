import jdk.nashorn.internal.runtime.JSType;

interface JSObject {
    /**
     * Returns this object's numeric value.
     *
     * @return this object's numeric value.
     * @deprecated use {@link #getDefaultValue(Class)} with {@link Number} hint instead.
     */
    @Deprecated
    default double toNumber() {
	return JSType.toNumber(JSType.toPrimitive(this, Number.class));
    }

}

