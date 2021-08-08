import jdk.nashorn.internal.objects.annotations.Attribute;
import jdk.nashorn.internal.objects.annotations.Where;
import jdk.nashorn.internal.runtime.JSType;

class NativeMath extends ScriptObject {
    /**
     * ECMA 15.8.2.15 round(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return x rounded
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double round(final Object self, final Object x) {
	final double d = JSType.toNumber(x);
	if (Math.getExponent(d) &gt;= 52) {
	    return d;
	}
	return Math.copySign(Math.floor(d + 0.5), d);
    }

}

