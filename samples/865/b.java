import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;

class Variable extends VariableBase {
    /**
     * Parse the contents of the variable
     */
    public void parseContents(Parser parser) {
	// Parse 'name' and 'select' attributes plus parameter contents
	super.parseContents(parser);

	// Add a ref to this var to its enclosing construct
	SyntaxTreeNode parent = getParent();
	if (parent instanceof Stylesheet) {
	    // Mark this as a global variable
	    _isLocal = false;
	    // Check if a global variable with this name already exists...
	    Variable var = parser.getSymbolTable().lookupVariable(_name);
	    // ...and if it does we need to check import precedence
	    if (var != null) {
		final int us = this.getImportPrecedence();
		final int them = var.getImportPrecedence();
		// It is an error if the two have the same import precedence
		if (us == them) {
		    final String name = _name.toString();
		    reportError(this, parser, ErrorMsg.VARIABLE_REDEF_ERR, name);
		}
		// Ignore this if previous definition has higher precedence
		else if (them &gt; us) {
		    _ignore = true;
		    copyReferences(var);
		    return;
		} else {
		    var.copyReferences(this);
		    var.disable();
		}
		// Add this variable if we have higher precedence
	    }
	    ((Stylesheet) parent).addVariable(this);
	    parser.getSymbolTable().addVariable(this);
	} else {
	    _isLocal = true;
	}
    }

}

