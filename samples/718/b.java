import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.jvm.Target;

class Lower extends TreeTranslator {
    /** The name of the access method with number `anum' and access code `acode'.
     */
    Name accessName(int anum, int acode) {
	return names.fromString("access" + target.syntheticNameChar() + anum + acode / 10 + acode % 10);
    }

    private final Names names;
    private final Target target;

}

