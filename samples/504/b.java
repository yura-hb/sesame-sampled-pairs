import java.util.*;

class DocEnv {
    /**
     * Return the PackageDoc of this package symbol.
     */
    public PackageDocImpl getPackageDoc(PackageSymbol pack) {
	PackageDocImpl result = packageMap.get(pack);
	if (result != null)
	    return result;
	result = new PackageDocImpl(this, pack);
	packageMap.put(pack, result);
	return result;
    }

    protected Map&lt;PackageSymbol, PackageDocImpl&gt; packageMap = new HashMap&lt;&gt;();

}

