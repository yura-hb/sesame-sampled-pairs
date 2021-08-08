import java.awt.Component;

class SpringUtilities {
    /**
     * A debugging utility that prints to stdout the component's minimum,
     * preferred, and maximum sizes.
     */
    public static void printSizes(Component c) {
	System.out.println("minimumSize = " + c.getMinimumSize());
	System.out.println("preferredSize = " + c.getPreferredSize());
	System.out.println("maximumSize = " + c.getMaximumSize());
    }

}

