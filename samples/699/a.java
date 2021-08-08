class FilerTesterProc extends AbstractProcessor {
    /**
     * @return a string representing a large Java class.
     */
    public static String largeJavaClass() {
	StringBuffer sb = new StringBuffer();
	sb.append("package g;\n");
	sb.append("public class Test {\n");
	sb.append("    public static final String bigString = \n");
	for (int i = 0; i &lt; 500; ++i) {
	    sb.append("        \"the quick brown dog jumped over the lazy fox, in a peculiar reversal\\n\" +\n");
	}
	sb.append("    \"\";\n");
	sb.append("\n");
	sb.append("    /** This file is at least this big */\n");
	sb.append("    public static final int SIZE = ");
	sb.append(sb.length());
	sb.append(";\n");
	sb.append("}\n");
	return sb.toString();
    }

}

