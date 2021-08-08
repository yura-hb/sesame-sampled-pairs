import org.eclipse.jdt.internal.compiler.lookup.SplitPackageBinding;

class Factory {
    /**
     * Convenience method - equivalent to {@code (PackageElement)Factory.newElement(binding)}
     */
    public PackageElement newPackageElement(PackageBinding binding) {
	if (binding instanceof SplitPackageBinding && binding.enclosingModule != null) {
	    binding = ((SplitPackageBinding) binding).getIncarnation(binding.enclosingModule);
	}
	if (binding == null) {
	    return null;
	}
	return new PackageElementImpl(_env, binding);
    }

    private final BaseProcessingEnvImpl _env;

}

