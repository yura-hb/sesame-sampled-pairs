import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.TypeVector;

class TypeHierarchy implements ITypeHierarchy, IElementChangedListener {
    /**
    * Returns whether the simple name of a supertype of the given type is
    * the simple name of one of the subtypes in this hierarchy or the
    * simple name of this type.
    */
    boolean subtypesIncludeSupertypeOf(IType type) {
	// look for superclass
	String superclassName = null;
	try {
	    superclassName = type.getSuperclassName();
	} catch (JavaModelException e) {
	    if (DEBUG) {
		e.printStackTrace();
	    }
	    return false;
	}
	if (superclassName == null) {
	    superclassName = "Object"; //$NON-NLS-1$
	}
	if (hasSubtypeNamed(superclassName)) {
	    return true;
	}

	// look for super interfaces
	String[] interfaceNames = null;
	try {
	    interfaceNames = type.getSuperInterfaceNames();
	} catch (JavaModelException e) {
	    if (DEBUG)
		e.printStackTrace();
	    return false;
	}
	for (int i = 0, length = interfaceNames.length; i &lt; length; i++) {
	    String interfaceName = interfaceNames[i];
	    if (hasSubtypeNamed(interfaceName)) {
		return true;
	    }
	}

	return false;
    }

    public static boolean DEBUG = false;
    /**
     * The type the hierarchy was specifically computed for,
     * possibly null.
     */
    protected IType focusType;
    protected TypeVector rootClasses = new TypeVector();
    protected Map&lt;IType, IType&gt; classToSuperclass;
    protected ArrayList&lt;IType&gt; interfaces = new ArrayList&lt;IType&gt;(10);
    protected Map&lt;IType, TypeVector&gt; typeToSubtypes;
    protected static final IType[] NO_TYPE = new IType[0];

    /**
    * Returns whether this type or one of the subtypes in this hierarchy has the
    * same simple name as the given name.
    */
    private boolean hasSubtypeNamed(String name) {
	int idx = -1;
	String rawName = (idx = name.indexOf('&lt;')) &gt; -1 ? name.substring(0, idx) : name;
	String simpleName = (idx = rawName.lastIndexOf('.')) &gt; -1 ? rawName.substring(idx + 1) : rawName;

	if (this.focusType != null && this.focusType.getElementName().equals(simpleName)) {
	    return true;
	}
	IType[] types = this.focusType == null ? getAllTypes() : getAllSubtypes(this.focusType);
	for (int i = 0, length = types.length; i &lt; length; i++) {
	    if (types[i].getElementName().equals(simpleName)) {
		return true;
	    }
	}
	return false;
    }

    /**
    * @see ITypeHierarchy
    */
    @Override
    public IType[] getAllTypes() {
	IType[] classes = getAllClasses();
	int classesLength = classes.length;
	IType[] allInterfaces = getAllInterfaces();
	int interfacesLength = allInterfaces.length;
	IType[] all = new IType[classesLength + interfacesLength];
	System.arraycopy(classes, 0, all, 0, classesLength);
	System.arraycopy(allInterfaces, 0, all, classesLength, interfacesLength);
	return all;
    }

    /**
    * @see ITypeHierarchy
    */
    @Override
    public IType[] getAllSubtypes(IType type) {
	return getAllSubtypesForType(type);
    }

    /**
    * @see ITypeHierarchy
    */
    @Override
    public IType[] getAllClasses() {

	TypeVector classes = this.rootClasses.copy();
	for (Iterator&lt;IType&gt; iter = this.classToSuperclass.keySet().iterator(); iter.hasNext();) {
	    classes.add(iter.next());
	}
	return classes.elements();
    }

    /**
    * @see ITypeHierarchy
    */
    @Override
    public IType[] getAllInterfaces() {
	IType[] collection = new IType[this.interfaces.size()];
	this.interfaces.toArray(collection);
	return collection;
    }

    /**
    * @see #getAllSubtypes(IType)
    */
    private IType[] getAllSubtypesForType(IType type) {
	ArrayList&lt;IType&gt; subTypes = new ArrayList&lt;&gt;();
	getAllSubtypesForType0(type, subTypes);
	IType[] subClasses = new IType[subTypes.size()];
	subTypes.toArray(subClasses);
	return subClasses;
    }

    /**
    */
    private void getAllSubtypesForType0(IType type, ArrayList&lt;IType&gt; subs) {
	IType[] subTypes = getSubtypesForType(type);
	if (subTypes.length != 0) {
	    for (int i = 0; i &lt; subTypes.length; i++) {
		IType subType = subTypes[i];
		if (subs.contains(subType))
		    continue;
		subs.add(subType);
		getAllSubtypesForType0(subType, subs);
	    }
	}
    }

    /**
    * Returns an array of subtypes for the given type - will never return null.
    */
    private IType[] getSubtypesForType(IType type) {
	TypeVector vector = this.typeToSubtypes.get(type);
	if (vector == null)
	    return NO_TYPE;
	else
	    return vector.elements();
    }

}

