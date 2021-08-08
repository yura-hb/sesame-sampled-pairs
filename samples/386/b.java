import com.sun.tools.javac.code.*;
import com.sun.tools.javac.jvm.Items.*;

class Gen extends Visitor {
    /** Generate code to load an integer constant.
     *  @param n     The integer to be loaded.
     */
    void loadIntConst(int n) {
	items.makeImmediateItem(syms.intType, n).load();
    }

    /** Items structure, set by genMethod.
     */
    private Items items;
    private final Symtab syms;

}

