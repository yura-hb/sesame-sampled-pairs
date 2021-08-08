import java.io.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Flags.ANNOTATION;

class Pretty extends Visitor {
    /** Print a set of modifiers.
     */
    public void printFlags(long flags) throws IOException {
	if ((flags & SYNTHETIC) != 0)
	    print("/*synthetic*/ ");
	print(TreeInfo.flagNames(flags));
	if ((flags & ExtendedStandardFlags) != 0)
	    print(" ");
	if ((flags & ANNOTATION) != 0)
	    print("@");
    }

    /** The output stream on which trees are printed.
     */
    Writer out;

    /** Print string, replacing all non-ascii character with unicode escapes.
     */
    public void print(Object s) throws IOException {
	out.write(Convert.escapeUnicode(s.toString()));
    }

}

