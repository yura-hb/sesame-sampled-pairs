import java.nio.file.*;
import java.nio.channels.*;
import java.util.*;
import java.security.AccessController;

abstract class UnixFileStore extends FileStore {
    /**
     * Returns status to indicate if file system supports a given feature
     */
    FeatureStatus checkIfFeaturePresent(String feature) {
	if (props == null) {
	    synchronized (loadLock) {
		if (props == null) {
		    props = AccessController.doPrivileged(new PrivilegedAction&lt;&gt;() {
			@Override
			public Properties run() {
			    return loadProperties();
			}
		    });
		}
	    }
	}

	String value = props.getProperty(type());
	if (value != null) {
	    String[] values = value.split("\\s");
	    for (String s : values) {
		s = s.trim().toLowerCase();
		if (s.equals(feature)) {
		    return FeatureStatus.PRESENT;
		}
		if (s.startsWith("no")) {
		    s = s.substring(2);
		    if (s.equals(feature)) {
			return FeatureStatus.NOT_PRESENT;
		    }
		}
	    }
	}
	return FeatureStatus.UNKNOWN;
    }

    private static volatile Properties props;
    private static final Object loadLock = new Object();
    private final UnixMountEntry entry;

    private static Properties loadProperties() {
	Properties result = new Properties();
	String fstypes = StaticProperty.javaHome() + "/lib/fstypes.properties";
	Path file = Path.of(fstypes);
	try {
	    try (ReadableByteChannel rbc = Files.newByteChannel(file)) {
		result.load(Channels.newReader(rbc, "UTF-8"));
	    }
	} catch (IOException x) {
	}
	return result;
    }

    @Override
    public String type() {
	return entry.fstype();
    }

}

