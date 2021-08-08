import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

class Imports implements Constants {
    /**
     * Check the names of the imports.
     */
    public synchronized void resolve(Environment env) {
	if (checked != 0) {
	    return;
	}
	checked = -1;

	// After all class information has been read, now we can
	// safely inspect import information for errors.
	// If we did this before all parsing was finished,
	// we could get vicious circularities, since files can
	// import each others' classes.

	// A note: the resolution of the package java.lang takes place
	// in the sun.tools.javac.BatchEnvironment#setExemptPackages().

	// Make sure that the current package's name does not collide
	// with the name of an existing class. (bug 4101529)
	//
	// This change has been backed out because, on WIN32, it
	// failed to distinguish between java.awt.event and
	// java.awt.Event when looking for a directory.  We will
	// add this back in later.
	//
	// if (currentPackage != idNull) {
	//    Identifier resolvedName =
	//      env.resolvePackageQualifiedName(currentPackage);
	//
	//   Identifier className = resolvedName.getTopName();
	//
	//   if (importable(className, env)) {
	//      // The name of the current package is also the name
	//      // of a class.
	//      env.error(currentPackageWhere, "package.class.conflict",
	//                currentPackage, className);
	//     }
	// }

	Vector&lt;IdentifierToken&gt; resolvedPackages = new Vector&lt;&gt;();
	for (Enumeration&lt;IdentifierToken&gt; e = packages.elements(); e.hasMoreElements();) {
	    IdentifierToken t = e.nextElement();
	    Identifier nm = t.getName();
	    long where = t.getWhere();

	    // Check to see if this package is exempt from the "exists"
	    // check.  See the note in
	    // sun.tools.javac.BatchEnvironment#setExemptPackages()
	    // for more information.
	    if (env.isExemptPackage(nm)) {
		resolvedPackages.addElement(t);
		continue;
	    }

	    // (Note: This code is moved from BatchParser.importPackage().)
	    try {
		Identifier rnm = env.resolvePackageQualifiedName(nm);
		if (importable(rnm, env)) {
		    // This name is a real class; better not be a package too.
		    if (env.getPackage(rnm.getTopName()).exists()) {
			env.error(where, "class.and.package", rnm.getTopName());
		    }
		    // Pass an "inner" name to the imports.
		    if (!rnm.isInner())
			rnm = Identifier.lookupInner(rnm, idNull);
		    nm = rnm;
		} else if (!env.getPackage(nm).exists()) {
		    env.error(where, "package.not.found", nm, "import");
		} else if (rnm.isInner()) {
		    // nm exists, and rnm.getTopName() is a parent package
		    env.error(where, "class.and.package", rnm.getTopName());
		}
		resolvedPackages.addElement(new IdentifierToken(where, nm));
	    } catch (IOException ee) {
		env.error(where, "io.exception", "import");
	    }
	}
	packages = resolvedPackages;

	for (Enumeration&lt;IdentifierToken&gt; e = singles.elements(); e.hasMoreElements();) {
	    IdentifierToken t = e.nextElement();
	    Identifier nm = t.getName();
	    long where = t.getWhere();
	    Identifier pkg = nm.getQualifier();

	    // (Note: This code is moved from BatchParser.importClass().)
	    nm = env.resolvePackageQualifiedName(nm);
	    if (!env.classExists(nm.getTopName())) {
		env.error(where, "class.not.found", nm, "import");
	    }

	    // (Note: This code is moved from Imports.addClass().)
	    Identifier snm = nm.getFlatName().getName();

	    // make sure it isn't already imported explicitly
	    Identifier className = classes.get(snm);
	    if (className != null) {
		Identifier f1 = Identifier.lookup(className.getQualifier(), className.getFlatName());
		Identifier f2 = Identifier.lookup(nm.getQualifier(), nm.getFlatName());
		if (!f1.equals(f2)) {
		    env.error(where, "ambig.class", nm, className);
		}
	    }
	    classes.put(snm, nm);

	    // The code here needs to check to see, if we
	    // are importing an inner class, that all of its
	    // enclosing classes are visible to us.  To check this,
	    // we need to construct a definition for the class.
	    // The code here used to call...
	    //
	    //     ClassDefinition def = env.getClassDefinition(nm);
	    //
	    // ...but that interfered with the basicCheck()'ing of
	    // interfaces in certain cases (bug no. 4086139).  Never
	    // fear.  Instead we load the class with a call to the
	    // new getClassDefinitionNoCheck() which does no basicCheck() and
	    // lets us answer the questions we are interested in w/o
	    // interfering with the demand-driven nature of basicCheck().

	    try {
		// Get a declaration
		ClassDeclaration decl = env.getClassDeclaration(nm);

		// Get the definition (no env argument)
		ClassDefinition def = decl.getClassDefinitionNoCheck(env);

		// Get the true name of the package containing this class.
		// `pkg' from above is insufficient.  It includes the
		// names of our enclosing classes.  Fix for 4086815.
		Identifier importedPackage = def.getName().getQualifier();

		// Walk out the outerClass chain, ensuring that each level
		// is visible from our perspective.
		for (; def != null; def = def.getOuterClass()) {
		    if (def.isPrivate() || !(def.isPublic() || importedPackage.equals(currentPackage))) {
			env.error(where, "cant.access.class", def);
			break;
		    }
		}
	    } catch (AmbiguousClass ee) {
		env.error(where, "ambig.class", ee.name1, ee.name2);
	    } catch (ClassNotFound ee) {
		env.error(where, "class.not.found", ee.name, "import");
	    }
	}
	checked = 1;
    }

    /**
     * Are the import names checked yet?
     */
    protected int checked;
    /**
     * The imported package identifiers.  This will not contain duplicate
     * imports for the same package.  It will also not contain the
     * current package.
     */
    Vector&lt;IdentifierToken&gt; packages = new Vector&lt;&gt;();
    /**
     * The (originally) imported classes.
     * A vector of IdentifierToken.
     */
    Vector&lt;IdentifierToken&gt; singles = new Vector&lt;&gt;();
    /**
     * The imported classes, including memoized imports from packages.
     */
    Hashtable&lt;Identifier, Identifier&gt; classes = new Hashtable&lt;&gt;();
    /**
     * The current package, which is implicitly imported,
     * and has precedence over other imported packages.
     */
    Identifier currentPackage = idNull;

    /**
     * Check to see if 'id' names an importable class in `env'.
     * This method was made public and static for utility.
     */
    static public boolean importable(Identifier id, Environment env) {
	if (!id.isInner()) {
	    return env.classExists(id);
	} else if (!env.classExists(id.getTopName())) {
	    return false;
	} else {
	    // load the top class and look inside it
	    try {
		// There used to be a call to...
		//    env.getClassDeclaration(id.getTopName());
		// ...here.  It has been replaced with the
		// two statements below.  These should be functionally
		// the same except for the fact that
		// getClassDefinitionNoCheck() does not call
		// basicCheck().  This allows us to avoid a circular
		// need to do basicChecking that can arise with
		// certain patterns of importing and inheritance.
		// This is a fix for a variant of bug 4086139.
		//
		// Note: the special case code in env.getClassDefinition()
		// which handles inner class names is not replicated below.
		// This should be okay, as we are looking up id.getTopName(),
		// not id.
		ClassDeclaration decl = env.getClassDeclaration(id.getTopName());
		ClassDefinition c = decl.getClassDefinitionNoCheck(env);

		return c.innerClassExists(id.getFlatName().getTail());
	    } catch (ClassNotFound ee) {
		return false;
	    }
	}
    }

}

