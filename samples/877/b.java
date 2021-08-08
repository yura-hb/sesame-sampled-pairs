import java.util.Map;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.DocletEnvironment.ModuleMode;

class ElementsTable {
    /**
     * Returns the module documentation level mode.
     * @return the module documentation level mode
     */
    public ModuleMode getModuleMode() {
	switch (accessFilter.getAccessValue(ElementKind.MODULE)) {
	case PACKAGE:
	case PRIVATE:
	    return DocletEnvironment.ModuleMode.ALL;
	default:
	    return DocletEnvironment.ModuleMode.API;
	}
    }

    private final ModifierFilter accessFilter;

    class ModifierFilter {
	private final ModifierFilter accessFilter;

	public AccessKind getAccessValue(ElementKind kind) {
	    if (!ALLOWED_KINDS.contains(kind)) {
		throw new IllegalArgumentException("not allowed: " + kind);
	    }
	    return accessMap.getOrDefault(kind, AccessKind.PROTECTED);
	}

    }

}

