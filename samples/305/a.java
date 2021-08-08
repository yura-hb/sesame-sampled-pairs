import java.util.Map;
import org.eclipse.jdt.internal.compiler.util.SimpleSetOfCharArray;

class ModuleBinding extends Binding implements IUpdatableModule {
    /** @return array of names, which may contain nulls. */
    public char[][] getPackageNamesForClassFile() {
	if (this.packageNames == null)
	    return null;
	for (PackageBinding packageBinding : this.exportedPackages)
	    this.packageNames.add(packageBinding.readableName());
	for (PackageBinding packageBinding : this.openedPackages)
	    this.packageNames.add(packageBinding.readableName());
	if (this.implementations != null)
	    for (TypeBinding[] types : this.implementations.values())
		for (TypeBinding typeBinding : types)
		    this.packageNames.add(((ReferenceBinding) typeBinding).fPackage.readableName());
	return this.packageNames.values;
    }

    private SimpleSetOfCharArray packageNames;
    protected PackageBinding[] exportedPackages;
    protected PackageBinding[] openedPackages;
    public Map&lt;TypeBinding, TypeBinding[]&gt; implementations;

}

