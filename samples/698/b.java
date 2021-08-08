import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;

interface IModuleAwareNameEnvironment {
    class LookupStrategy extends Enum&lt;LookupStrategy&gt; {
	/** Get the lookup strategy corresponding to the given module name. */
	public static LookupStrategy get(char[] moduleName) {
	    if (moduleName == ModuleBinding.ANY)
		return Any;
	    if (moduleName == ModuleBinding.ANY_NAMED)
		return AnyNamed;
	    if (moduleName == ModuleBinding.UNNAMED)
		return Unnamed;
	    return Named;
	}

    }

}

