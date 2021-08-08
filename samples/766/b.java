import static com.sun.tools.javac.jvm.ByteCodes.*;

class Code {
    /** The width in bytes of objects of the type.
     */
    public static int width(int typecode) {
	switch (typecode) {
	case LONGcode:
	case DOUBLEcode:
	    return 2;
	case VOIDcode:
	    return 0;
	default:
	    return 1;
	}
    }

}

