import java.io.File;

class Util implements SuffixConstants {
    /**
     * Returns the kind of archive this file is. The format is one of
     * #ZIP_FILE or {@link #JMOD_FILE}
     */
    public final static int archiveFormat(String name) {
	int lastDot = name.lastIndexOf('.');
	if (lastDot == -1)
	    return -1; // no file extension, it cannot be a zip archive name
	if (name.lastIndexOf(File.separatorChar) &gt; lastDot)
	    return -1; // dot was before the last file separator, it cannot be a zip archive name
	int length = name.length();
	int extensionLength = length - lastDot - 1;

	if (extensionLength == EXTENSION_java.length()) {
	    for (int i = extensionLength - 1; i &gt;= 0; i--) {
		if (Character.toLowerCase(name.charAt(length - extensionLength + i)) != EXTENSION_java.charAt(i)) {
		    break; // not a ".java" file, check ".class" file case below
		}
		if (i == 0) {
		    return -1; // it is a ".java" file, it cannot be a zip archive name
		}
	    }
	}
	if (extensionLength == EXTENSION_class.length()) {
	    for (int i = extensionLength - 1; i &gt;= 0; i--) {
		if (Character.toLowerCase(name.charAt(length - extensionLength + i)) != EXTENSION_class.charAt(i)) {
		    return ZIP_FILE; // not a ".class" file, so this is a potential archive name
		}
	    }
	    return -1; // it is a ".class" file, it cannot be a zip archive name
	}
	if (extensionLength == EXTENSION_jmod.length()) {
	    for (int i = extensionLength - 1; i &gt;= 0; i--) {
		if (Character.toLowerCase(name.charAt(length - extensionLength + i)) != EXTENSION_jmod.charAt(i)) {
		    return ZIP_FILE; // not a ".jmod" file, so this is a potential archive name
		}
	    }
	    return JMOD_FILE;
	}
	return ZIP_FILE; // it is neither a ".java" file nor a ".class" file, so this is a potential archive name
    }

    public static final int ZIP_FILE = 0;
    public static final int JMOD_FILE = 1;

}

