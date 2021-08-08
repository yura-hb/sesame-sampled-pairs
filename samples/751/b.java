abstract class ProcessUtil {
    /**
     * Print a formatted string to System.out.
     * @param format the format
     * @param args the argument array
     */
    static void printf(String format, Object... args) {
	String s = String.format(format, args);
	System.out.print(s);
    }

}

