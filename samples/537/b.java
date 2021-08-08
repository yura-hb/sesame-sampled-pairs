import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.module.ModuleDescriptor;
import java.util.Set;
import jdk.internal.module.ModuleInfoExtender;

class SystemModulesPlugin implements Plugin {
    class ModuleInfo {
	/**
	 * Returns the bytes for the (possibly updated) module-info.class.
	 */
	byte[] getBytes() throws IOException {
	    try (InputStream in = getInputStream()) {
		if (shouldRewrite()) {
		    ModuleInfoRewriter rewriter = new ModuleInfoRewriter(in);
		    if (addModulePackages) {
			rewriter.addModulePackages(packages);
		    }
		    // rewritten module descriptor
		    byte[] bytes = rewriter.getBytes();
		    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			this.descriptor = ModuleDescriptor.read(bais);
		    }
		    return bytes;
		} else {
		    return in.readAllBytes();
		}
	    }
	}

	private final boolean addModulePackages;
	private final Set&lt;String&gt; packages;
	private ModuleDescriptor descriptor;
	private final ByteArrayInputStream bais;

	InputStream getInputStream() {
	    bais.reset();
	    return bais;
	}

	/**
	 * Returns true if module-info.class should be rewritten to add the
	 * ModulePackages attribute.
	 */
	boolean shouldRewrite() {
	    return addModulePackages;
	}

	class ModuleInfoRewriter extends ByteArrayOutputStream {
	    private final boolean addModulePackages;
	    private final Set&lt;String&gt; packages;
	    private ModuleDescriptor descriptor;
	    private final ByteArrayInputStream bais;

	    ModuleInfoRewriter(InputStream in) {
		this.extender = ModuleInfoExtender.newExtender(in);
	    }

	    void addModulePackages(Set&lt;String&gt; packages) {
		// Add ModulePackages attribute
		if (packages.size() &gt; 0) {
		    extender.packages(packages);
		}
	    }

	    byte[] getBytes() throws IOException {
		extender.write(this);
		return buf;
	    }

	}

    }

}

