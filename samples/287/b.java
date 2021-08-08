import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Version;

class Builder {
    /**
     * Sets the module version.
     *
     * @throws IllegalArgumentException if {@code v} is null or cannot be
     *         parsed as a version string
     *
     * @see Version#parse(String)
     */
    public Builder version(String v) {
	Version ver = cachedVersion;
	if (ver != null && v.equals(ver.toString())) {
	    version = ver;
	} else {
	    cachedVersion = version = Version.parse(v);
	}
	return this;
    }

    static Version cachedVersion;
    Version version;

}

