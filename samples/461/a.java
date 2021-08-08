class JRTUtil {
    /** TEST ONLY (use when changing the "modules.to.load" property). */
    public static void reset() {
	images = null;
	MODULE_TO_LOAD = System.getProperty("modules.to.load"); //$NON-NLS-1$
    }

    private static Map&lt;File, JrtFileSystem&gt; images = null;
    static String MODULE_TO_LOAD = null;

}

