import java.util.HashMap;
import java.util.Map;

class SymbolTable {
    /**
     * Adds an alias for a namespace prefix
     */
    public void addPrefixAlias(String prefix, String alias) {
	if (_aliases == null)
	    _aliases = new HashMap&lt;&gt;();
	_aliases.put(prefix, alias);
    }

    private Map&lt;String, String&gt; _aliases = null;

}

