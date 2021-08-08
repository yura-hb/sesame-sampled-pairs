import java.io.DataInput;
import java.io.DataInputStream;
import java.io.UncheckedIOException;
import java.lang.module.InvalidModuleDescriptorException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Builder;
import java.lang.module.ModuleDescriptor.Requires;
import java.lang.module.ModuleDescriptor.Exports;
import java.lang.module.ModuleDescriptor.Opens;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jdk.internal.misc.JavaLangModuleAccess;
import static jdk.internal.module.ClassFileConstants.*;

class ModuleInfo {
    /**
     * Reads a {@code module-info.class} from the given input stream.
     *
     * @throws InvalidModuleDescriptorException
     * @throws IOException
     */
    public static Attributes read(InputStream in, Supplier&lt;Set&lt;String&gt;&gt; pf) throws IOException {
	try {
	    return new ModuleInfo(pf).doRead(new DataInputStream(in));
	} catch (IllegalArgumentException | IllegalStateException e) {
	    throw invalidModuleDescriptor(e.getMessage());
	} catch (EOFException x) {
	    throw truncatedModuleDescriptor();
	}
    }

    private final int JAVA_MIN_SUPPORTED_VERSION = 53;
    private final int JAVA_MAX_SUPPORTED_VERSION = 55;
    private final boolean parseHashes;
    private final Supplier&lt;Set&lt;String&gt;&gt; packageFinder;
    private static final JavaLangModuleAccess JLMA = SharedSecrets.getJavaLangModuleAccess();
    private static volatile Set&lt;String&gt; predefinedNotAllowed;

    private ModuleInfo(Supplier&lt;Set&lt;String&gt;&gt; pf) {
	this(pf, true);
    }

    /**
     * Reads the input as a module-info class file.
     *
     * @throws IOException
     * @throws InvalidModuleDescriptorException
     * @throws IllegalArgumentException if thrown by the ModuleDescriptor.Builder
     *         because an identifier is not a legal Java identifier, duplicate
     *         exports, and many other reasons
     */
    private Attributes doRead(DataInput in) throws IOException {

	int magic = in.readInt();
	if (magic != 0xCAFEBABE)
	    throw invalidModuleDescriptor("Bad magic number");

	int minor_version = in.readUnsignedShort();
	int major_version = in.readUnsignedShort();
	if (major_version &lt; JAVA_MIN_SUPPORTED_VERSION || major_version &gt; JAVA_MAX_SUPPORTED_VERSION) {
	    throw invalidModuleDescriptor("Unsupported major.minor version " + major_version + "." + minor_version);
	}

	ConstantPool cpool = new ConstantPool(in);

	int access_flags = in.readUnsignedShort();
	if (access_flags != ACC_MODULE)
	    throw invalidModuleDescriptor("access_flags should be ACC_MODULE");

	int this_class = in.readUnsignedShort();
	String mn = cpool.getClassName(this_class);
	if (!"module-info".equals(mn))
	    throw invalidModuleDescriptor("this_class should be module-info");

	int super_class = in.readUnsignedShort();
	if (super_class &gt; 0)
	    throw invalidModuleDescriptor("bad #super_class");

	int interfaces_count = in.readUnsignedShort();
	if (interfaces_count &gt; 0)
	    throw invalidModuleDescriptor("Bad #interfaces");

	int fields_count = in.readUnsignedShort();
	if (fields_count &gt; 0)
	    throw invalidModuleDescriptor("Bad #fields");

	int methods_count = in.readUnsignedShort();
	if (methods_count &gt; 0)
	    throw invalidModuleDescriptor("Bad #methods");

	int attributes_count = in.readUnsignedShort();

	// the names of the attributes found in the class file
	Set&lt;String&gt; attributes = new HashSet&lt;&gt;();

	Builder builder = null;
	Set&lt;String&gt; allPackages = null;
	String mainClass = null;
	ModuleTarget moduleTarget = null;
	ModuleHashes moduelHashes = null;
	ModuleResolution moduleResolution = null;

	for (int i = 0; i &lt; attributes_count; i++) {
	    int name_index = in.readUnsignedShort();
	    String attribute_name = cpool.getUtf8(name_index);
	    int length = in.readInt();

	    boolean added = attributes.add(attribute_name);
	    if (!added && isAttributeAtMostOnce(attribute_name)) {
		throw invalidModuleDescriptor("More than one " + attribute_name + " attribute");
	    }

	    switch (attribute_name) {

	    case MODULE:
		builder = readModuleAttribute(in, cpool, major_version);
		break;

	    case MODULE_PACKAGES:
		allPackages = readModulePackagesAttribute(in, cpool);
		break;

	    case MODULE_MAIN_CLASS:
		mainClass = readModuleMainClassAttribute(in, cpool);
		break;

	    case MODULE_TARGET:
		moduleTarget = readModuleTargetAttribute(in, cpool);
		break;

	    case MODULE_HASHES:
		if (parseHashes) {
		    moduelHashes = readModuleHashesAttribute(in, cpool);
		} else {
		    in.skipBytes(length);
		}
		break;

	    case MODULE_RESOLUTION:
		moduleResolution = readModuleResolution(in, cpool);
		break;

	    default:
		if (isAttributeDisallowed(attribute_name)) {
		    throw invalidModuleDescriptor(attribute_name + " attribute not allowed");
		} else {
		    in.skipBytes(length);
		}

	    }
	}

	// the Module attribute is required
	if (builder == null) {
	    throw invalidModuleDescriptor(MODULE + " attribute not found");
	}

	// ModuleMainClass  attribute
	if (mainClass != null) {
	    builder.mainClass(mainClass);
	}

	// If the ModulePackages attribute is not present then the packageFinder
	// is used to find the set of packages
	boolean usedPackageFinder = false;
	if (allPackages == null && packageFinder != null) {
	    try {
		allPackages = packageFinder.get();
	    } catch (UncheckedIOException x) {
		throw x.getCause();
	    }
	    usedPackageFinder = true;
	}
	if (allPackages != null) {
	    Set&lt;String&gt; knownPackages = JLMA.packages(builder);
	    if (!allPackages.containsAll(knownPackages)) {
		Set&lt;String&gt; missingPackages = new HashSet&lt;&gt;(knownPackages);
		missingPackages.removeAll(allPackages);
		assert !missingPackages.isEmpty();
		String missingPackage = missingPackages.iterator().next();
		String tail;
		if (usedPackageFinder) {
		    tail = " not found in module";
		} else {
		    tail = " missing from ModulePackages class file attribute";
		}
		throw invalidModuleDescriptor("Package " + missingPackage + tail);

	    }
	    builder.packages(allPackages);
	}

	ModuleDescriptor descriptor = builder.build();
	return new Attributes(descriptor, moduleTarget, moduelHashes, moduleResolution);
    }

    /**
     * Returns an InvalidModuleDescriptorException with the given detail
     * message
     */
    private static InvalidModuleDescriptorException invalidModuleDescriptor(String msg) {
	return new InvalidModuleDescriptorException(msg);
    }

    /**
     * Returns an InvalidModuleDescriptorException with a detail message to
     * indicate that the class file is truncated.
     */
    private static InvalidModuleDescriptorException truncatedModuleDescriptor() {
	return invalidModuleDescriptor("Truncated module-info.class");
    }

    private ModuleInfo(Supplier&lt;Set&lt;String&gt;&gt; pf, boolean ph) {
	packageFinder = pf;
	parseHashes = ph;
    }

    /**
     * Returns true if the given attribute can be present at most once
     * in the class file. Returns false otherwise.
     */
    private static boolean isAttributeAtMostOnce(String name) {

	if (name.equals(MODULE) || name.equals(SOURCE_FILE) || name.equals(SDE) || name.equals(MODULE_PACKAGES)
		|| name.equals(MODULE_MAIN_CLASS) || name.equals(MODULE_TARGET) || name.equals(MODULE_HASHES)
		|| name.equals(MODULE_RESOLUTION))
	    return true;

	return false;
    }

    /**
     * Reads the Module attribute, returning the ModuleDescriptor.Builder to
     * build the corresponding ModuleDescriptor.
     */
    private Builder readModuleAttribute(DataInput in, ConstantPool cpool, int major) throws IOException {
	// module_name
	int module_name_index = in.readUnsignedShort();
	String mn = cpool.getModuleName(module_name_index);

	int module_flags = in.readUnsignedShort();

	Set&lt;ModuleDescriptor.Modifier&gt; modifiers = new HashSet&lt;&gt;();
	boolean open = ((module_flags & ACC_OPEN) != 0);
	if (open)
	    modifiers.add(ModuleDescriptor.Modifier.OPEN);
	if ((module_flags & ACC_SYNTHETIC) != 0)
	    modifiers.add(ModuleDescriptor.Modifier.SYNTHETIC);
	if ((module_flags & ACC_MANDATED) != 0)
	    modifiers.add(ModuleDescriptor.Modifier.MANDATED);

	Builder builder = JLMA.newModuleBuilder(mn, false, modifiers);

	int module_version_index = in.readUnsignedShort();
	if (module_version_index != 0) {
	    String vs = cpool.getUtf8(module_version_index);
	    builder.version(vs);
	}

	int requires_count = in.readUnsignedShort();
	boolean requiresJavaBase = false;
	for (int i = 0; i &lt; requires_count; i++) {
	    int requires_index = in.readUnsignedShort();
	    String dn = cpool.getModuleName(requires_index);

	    int requires_flags = in.readUnsignedShort();
	    Set&lt;Requires.Modifier&gt; mods;
	    if (requires_flags == 0) {
		mods = Collections.emptySet();
	    } else {
		mods = new HashSet&lt;&gt;();
		if ((requires_flags & ACC_TRANSITIVE) != 0)
		    mods.add(Requires.Modifier.TRANSITIVE);
		if ((requires_flags & ACC_STATIC_PHASE) != 0)
		    mods.add(Requires.Modifier.STATIC);
		if ((requires_flags & ACC_SYNTHETIC) != 0)
		    mods.add(Requires.Modifier.SYNTHETIC);
		if ((requires_flags & ACC_MANDATED) != 0)
		    mods.add(Requires.Modifier.MANDATED);
	    }

	    int requires_version_index = in.readUnsignedShort();
	    if (requires_version_index == 0) {
		builder.requires(mods, dn);
	    } else {
		String vs = cpool.getUtf8(requires_version_index);
		JLMA.requires(builder, mods, dn, vs);
	    }

	    if (dn.equals("java.base")) {
		if (major &gt;= 54
			&& (mods.contains(Requires.Modifier.TRANSITIVE) || mods.contains(Requires.Modifier.STATIC))) {
		    String flagName;
		    if (mods.contains(Requires.Modifier.TRANSITIVE)) {
			flagName = "ACC_TRANSITIVE";
		    } else {
			flagName = "ACC_STATIC_PHASE";
		    }
		    throw invalidModuleDescriptor("The requires entry for java.base" + " has " + flagName + " set");
		}
		requiresJavaBase = true;
	    }
	}
	if (mn.equals("java.base")) {
	    if (requires_count &gt; 0) {
		throw invalidModuleDescriptor("The requires table for java.base" + " must be 0 length");
	    }
	} else if (!requiresJavaBase) {
	    throw invalidModuleDescriptor("The requires table must have" + " an entry for java.base");
	}

	int exports_count = in.readUnsignedShort();
	if (exports_count &gt; 0) {
	    for (int i = 0; i &lt; exports_count; i++) {
		int exports_index = in.readUnsignedShort();
		String pkg = cpool.getPackageName(exports_index);

		Set&lt;Exports.Modifier&gt; mods;
		int exports_flags = in.readUnsignedShort();
		if (exports_flags == 0) {
		    mods = Collections.emptySet();
		} else {
		    mods = new HashSet&lt;&gt;();
		    if ((exports_flags & ACC_SYNTHETIC) != 0)
			mods.add(Exports.Modifier.SYNTHETIC);
		    if ((exports_flags & ACC_MANDATED) != 0)
			mods.add(Exports.Modifier.MANDATED);
		}

		int exports_to_count = in.readUnsignedShort();
		if (exports_to_count &gt; 0) {
		    Set&lt;String&gt; targets = new HashSet&lt;&gt;(exports_to_count);
		    for (int j = 0; j &lt; exports_to_count; j++) {
			int exports_to_index = in.readUnsignedShort();
			String target = cpool.getModuleName(exports_to_index);
			if (!targets.add(target)) {
			    throw invalidModuleDescriptor(pkg + " exported to " + target + " more than once");
			}
		    }
		    builder.exports(mods, pkg, targets);
		} else {
		    builder.exports(mods, pkg);
		}
	    }
	}

	int opens_count = in.readUnsignedShort();
	if (opens_count &gt; 0) {
	    if (open) {
		throw invalidModuleDescriptor("The opens table for an open" + " module must be 0 length");
	    }
	    for (int i = 0; i &lt; opens_count; i++) {
		int opens_index = in.readUnsignedShort();
		String pkg = cpool.getPackageName(opens_index);

		Set&lt;Opens.Modifier&gt; mods;
		int opens_flags = in.readUnsignedShort();
		if (opens_flags == 0) {
		    mods = Collections.emptySet();
		} else {
		    mods = new HashSet&lt;&gt;();
		    if ((opens_flags & ACC_SYNTHETIC) != 0)
			mods.add(Opens.Modifier.SYNTHETIC);
		    if ((opens_flags & ACC_MANDATED) != 0)
			mods.add(Opens.Modifier.MANDATED);
		}

		int open_to_count = in.readUnsignedShort();
		if (open_to_count &gt; 0) {
		    Set&lt;String&gt; targets = new HashSet&lt;&gt;(open_to_count);
		    for (int j = 0; j &lt; open_to_count; j++) {
			int opens_to_index = in.readUnsignedShort();
			String target = cpool.getModuleName(opens_to_index);
			if (!targets.add(target)) {
			    throw invalidModuleDescriptor(pkg + " opened to " + target + " more than once");
			}
		    }
		    builder.opens(mods, pkg, targets);
		} else {
		    builder.opens(mods, pkg);
		}
	    }
	}

	int uses_count = in.readUnsignedShort();
	if (uses_count &gt; 0) {
	    for (int i = 0; i &lt; uses_count; i++) {
		int index = in.readUnsignedShort();
		String sn = cpool.getClassName(index);
		builder.uses(sn);
	    }
	}

	int provides_count = in.readUnsignedShort();
	if (provides_count &gt; 0) {
	    for (int i = 0; i &lt; provides_count; i++) {
		int index = in.readUnsignedShort();
		String sn = cpool.getClassName(index);
		int with_count = in.readUnsignedShort();
		List&lt;String&gt; providers = new ArrayList&lt;&gt;(with_count);
		for (int j = 0; j &lt; with_count; j++) {
		    index = in.readUnsignedShort();
		    String pn = cpool.getClassName(index);
		    if (!providers.add(pn)) {
			throw invalidModuleDescriptor(sn + " provides " + pn + " more than once");
		    }
		}
		builder.provides(sn, providers);
	    }
	}

	return builder;
    }

    /**
     * Reads the ModulePackages attribute
     */
    private Set&lt;String&gt; readModulePackagesAttribute(DataInput in, ConstantPool cpool) throws IOException {
	int package_count = in.readUnsignedShort();
	Set&lt;String&gt; packages = new HashSet&lt;&gt;(package_count);
	for (int i = 0; i &lt; package_count; i++) {
	    int index = in.readUnsignedShort();
	    String pn = cpool.getPackageName(index);
	    boolean added = packages.add(pn);
	    if (!added) {
		throw invalidModuleDescriptor("Package " + pn + " in ModulePackages" + "attribute more than once");
	    }
	}
	return packages;
    }

    /**
     * Reads the ModuleMainClass attribute
     */
    private String readModuleMainClassAttribute(DataInput in, ConstantPool cpool) throws IOException {
	int index = in.readUnsignedShort();
	return cpool.getClassName(index);
    }

    /**
     * Reads the ModuleTarget attribute
     */
    private ModuleTarget readModuleTargetAttribute(DataInput in, ConstantPool cpool) throws IOException {
	String targetPlatform = null;

	int index = in.readUnsignedShort();
	if (index != 0)
	    targetPlatform = cpool.getUtf8(index);

	return new ModuleTarget(targetPlatform);
    }

    /**
     * Reads the ModuleHashes attribute
     */
    private ModuleHashes readModuleHashesAttribute(DataInput in, ConstantPool cpool) throws IOException {
	int algorithm_index = in.readUnsignedShort();
	String algorithm = cpool.getUtf8(algorithm_index);

	int hash_count = in.readUnsignedShort();
	Map&lt;String, byte[]&gt; map = new HashMap&lt;&gt;(hash_count);
	for (int i = 0; i &lt; hash_count; i++) {
	    int module_name_index = in.readUnsignedShort();
	    String mn = cpool.getModuleName(module_name_index);
	    int hash_length = in.readUnsignedShort();
	    if (hash_length == 0) {
		throw invalidModuleDescriptor("hash_length == 0");
	    }
	    byte[] hash = new byte[hash_length];
	    in.readFully(hash);
	    map.put(mn, hash);
	}

	return new ModuleHashes(algorithm, map);
    }

    /**
     * Reads the ModuleResolution attribute.
     */
    private ModuleResolution readModuleResolution(DataInput in, ConstantPool cpool) throws IOException {
	int flags = in.readUnsignedShort();

	int reason = 0;
	if ((flags & WARN_DEPRECATED) != 0)
	    reason = WARN_DEPRECATED;
	if ((flags & WARN_DEPRECATED_FOR_REMOVAL) != 0) {
	    if (reason != 0)
		throw invalidModuleDescriptor("Bad module resolution flags:" + flags);
	    reason = WARN_DEPRECATED_FOR_REMOVAL;
	}
	if ((flags & WARN_INCUBATING) != 0) {
	    if (reason != 0)
		throw invalidModuleDescriptor("Bad module resolution flags:" + flags);
	}

	return new ModuleResolution(flags);
    }

    /**
     * Return true if the given attribute name is the name of a pre-defined
     * attribute in JVMS 4.7 that is not allowed in a module-info class.
     */
    private static boolean isAttributeDisallowed(String name) {
	Set&lt;String&gt; notAllowed = predefinedNotAllowed;
	if (notAllowed == null) {
	    notAllowed = Set.of("ConstantValue", "Code", "Deprecated", "StackMapTable", "Exceptions", "EnclosingMethod",
		    "Signature", "LineNumberTable", "LocalVariableTable", "LocalVariableTypeTable",
		    "RuntimeVisibleParameterAnnotations", "RuntimeInvisibleParameterAnnotations",
		    "RuntimeVisibleTypeAnnotations", "RuntimeInvisibleTypeAnnotations", "Synthetic",
		    "AnnotationDefault", "BootstrapMethods", "MethodParameters");
	    predefinedNotAllowed = notAllowed;
	}
	return notAllowed.contains(name);
    }

    class ConstantPool {
	private final int JAVA_MIN_SUPPORTED_VERSION = 53;
	private final int JAVA_MAX_SUPPORTED_VERSION = 55;
	private final boolean parseHashes;
	private final Supplier&lt;Set&lt;String&gt;&gt; packageFinder;
	private static final JavaLangModuleAccess JLMA = SharedSecrets.getJavaLangModuleAccess();
	private static volatile Set&lt;String&gt; predefinedNotAllowed;

	ConstantPool(DataInput in) throws IOException {
	    int count = in.readUnsignedShort();
	    pool = new Entry[count];

	    for (int i = 1; i &lt; count; i++) {
		int tag = in.readUnsignedByte();
		switch (tag) {

		case CONSTANT_Utf8:
		    String svalue = in.readUTF();
		    pool[i] = new ValueEntry(tag, svalue);
		    break;

		case CONSTANT_Class:
		case CONSTANT_Package:
		case CONSTANT_Module:
		case CONSTANT_String:
		    int index = in.readUnsignedShort();
		    pool[i] = new IndexEntry(tag, index);
		    break;

		case CONSTANT_Double:
		    double dvalue = in.readDouble();
		    pool[i] = new ValueEntry(tag, dvalue);
		    i++;
		    break;

		case CONSTANT_Fieldref:
		case CONSTANT_InterfaceMethodref:
		case CONSTANT_Methodref:
		case CONSTANT_InvokeDynamic:
		case CONSTANT_NameAndType:
		    int index1 = in.readUnsignedShort();
		    int index2 = in.readUnsignedShort();
		    pool[i] = new Index2Entry(tag, index1, index2);
		    break;

		case CONSTANT_MethodHandle:
		    int refKind = in.readUnsignedByte();
		    index = in.readUnsignedShort();
		    pool[i] = new Index2Entry(tag, refKind, index);
		    break;

		case CONSTANT_MethodType:
		    index = in.readUnsignedShort();
		    pool[i] = new IndexEntry(tag, index);
		    break;

		case CONSTANT_Float:
		    float fvalue = in.readFloat();
		    pool[i] = new ValueEntry(tag, fvalue);
		    break;

		case CONSTANT_Integer:
		    int ivalue = in.readInt();
		    pool[i] = new ValueEntry(tag, ivalue);
		    break;

		case CONSTANT_Long:
		    long lvalue = in.readLong();
		    pool[i] = new ValueEntry(tag, lvalue);
		    i++;
		    break;

		default:
		    throw invalidModuleDescriptor("Bad constant pool entry: " + i);
		}
	    }
	}

	String getClassName(int index) {
	    checkIndex(index);
	    Entry e = pool[index];
	    if (e.tag != CONSTANT_Class) {
		throw invalidModuleDescriptor("CONSTANT_Class expected at entry: " + index);
	    }
	    String value = getUtf8(((IndexEntry) e).index);
	    checkUnqualifiedName("CONSTANT_Class", index, value);
	    return value.replace('/', '.'); // internal form -&gt; binary name
	}

	String getUtf8(int index) {
	    checkIndex(index);
	    Entry e = pool[index];
	    if (e.tag != CONSTANT_Utf8) {
		throw invalidModuleDescriptor("CONSTANT_Utf8 expected at entry: " + index);
	    }
	    return (String) (((ValueEntry) e).value);
	}

	void checkIndex(int index) {
	    if (index &lt; 1 || index &gt;= pool.length)
		throw invalidModuleDescriptor("Index into constant pool out of range");
	}

	void checkUnqualifiedName(String what, int index, String value) {
	    int len = value.length();
	    if (len == 0) {
		throw invalidModuleDescriptor(what + " at entry " + index + " has zero length");
	    }
	    for (int i = 0; i &lt; len; i++) {
		char c = value.charAt(i);
		if (c == '.' || c == ';' || c == '[') {
		    throw invalidModuleDescriptor(what + " at entry " + index + " has illegal character: '" + c + "'");
		}
	    }
	}

	String getModuleName(int index) {
	    checkIndex(index);
	    Entry e = pool[index];
	    if (e.tag != CONSTANT_Module) {
		throw invalidModuleDescriptor("CONSTANT_Module expected at entry: " + index);
	    }
	    String value = getUtf8(((IndexEntry) e).index);
	    return decodeModuleName(index, value);
	}

	String getPackageName(int index) {
	    checkIndex(index);
	    Entry e = pool[index];
	    if (e.tag != CONSTANT_Package) {
		throw invalidModuleDescriptor("CONSTANT_Package expected at entry: " + index);
	    }
	    String value = getUtf8(((IndexEntry) e).index);
	    checkUnqualifiedName("CONSTANT_Package", index, value);
	    return value.replace('/', '.'); // internal form -&gt; binary name
	}

	/**
	 * "Decode" a module name that has been read from the constant pool.
	 */
	String decodeModuleName(int index, String value) {
	    int len = value.length();
	    if (len == 0) {
		throw invalidModuleDescriptor("CONSTANT_Module at entry " + index + " is zero length");
	    }
	    int i = 0;
	    while (i &lt; len) {
		int cp = value.codePointAt(i);
		if (cp == ':' || cp == '@' || cp &lt; 0x20) {
		    throw invalidModuleDescriptor(
			    "CONSTANT_Module at entry " + index + " has illegal character: " + Character.getName(cp));
		}

		// blackslash is the escape character
		if (cp == '\\')
		    return decodeModuleName(index, i, value);

		i += Character.charCount(cp);
	    }
	    return value;
	}

	/**
	 * "Decode" a module name that has been read from the constant pool and
	 * partly checked for illegal characters (up to position {@code i}).
	 */
	String decodeModuleName(int index, int i, String value) {
	    StringBuilder sb = new StringBuilder();

	    // copy the code points that have been checked
	    int j = 0;
	    while (j &lt; i) {
		int cp = value.codePointAt(j);
		sb.appendCodePoint(cp);
		j += Character.charCount(cp);
	    }

	    // decode from position {@code i} to end
	    int len = value.length();
	    while (i &lt; len) {
		int cp = value.codePointAt(i);
		if (cp == ':' || cp == '@' || cp &lt; 0x20) {
		    throw invalidModuleDescriptor(
			    "CONSTANT_Module at entry " + index + " has illegal character: " + Character.getName(cp));
		}

		// blackslash is the escape character
		if (cp == '\\') {
		    j = i + Character.charCount(cp);
		    if (j &gt;= len) {
			throw invalidModuleDescriptor(
				"CONSTANT_Module at entry " + index + " has illegal " + "escape sequence");
		    }
		    int next = value.codePointAt(j);
		    if (next != '\\' && next != ':' && next != '@') {
			throw invalidModuleDescriptor(
				"CONSTANT_Module at entry " + index + " has illegal " + "escape sequence");
		    }
		    sb.appendCodePoint(next);
		    i += Character.charCount(next);
		} else {
		    sb.appendCodePoint(cp);
		}

		i += Character.charCount(cp);
	    }
	    return sb.toString();
	}

	class ValueEntry extends Entry {
	    final Entry[] pool;
	    static final int CONSTANT_Utf8 = 1;
	    static final int CONSTANT_Class = 7;
	    static final int CONSTANT_Package = 20;
	    static final int CONSTANT_Module = 19;
	    static final int CONSTANT_String = 8;
	    static final int CONSTANT_Double = 6;
	    static final int CONSTANT_Fieldref = 9;
	    static final int CONSTANT_InterfaceMethodref = 11;
	    static final int CONSTANT_Methodref = 10;
	    static final int CONSTANT_InvokeDynamic = 18;
	    static final int CONSTANT_NameAndType = 12;
	    static final int CONSTANT_MethodHandle = 15;
	    static final int CONSTANT_MethodType = 16;
	    static final int CONSTANT_Float = 4;
	    static final int CONSTANT_Integer = 3;
	    static final int CONSTANT_Long = 5;

	    ValueEntry(int tag, Object value) {
		super(tag);
		this.value = value;
	    }

	}

	class IndexEntry extends Entry {
	    final Entry[] pool;
	    static final int CONSTANT_Utf8 = 1;
	    static final int CONSTANT_Class = 7;
	    static final int CONSTANT_Package = 20;
	    static final int CONSTANT_Module = 19;
	    static final int CONSTANT_String = 8;
	    static final int CONSTANT_Double = 6;
	    static final int CONSTANT_Fieldref = 9;
	    static final int CONSTANT_InterfaceMethodref = 11;
	    static final int CONSTANT_Methodref = 10;
	    static final int CONSTANT_InvokeDynamic = 18;
	    static final int CONSTANT_NameAndType = 12;
	    static final int CONSTANT_MethodHandle = 15;
	    static final int CONSTANT_MethodType = 16;
	    static final int CONSTANT_Float = 4;
	    static final int CONSTANT_Integer = 3;
	    static final int CONSTANT_Long = 5;

	    IndexEntry(int tag, int index) {
		super(tag);
		this.index = index;
	    }

	}

	class Index2Entry extends Entry {
	    final Entry[] pool;
	    static final int CONSTANT_Utf8 = 1;
	    static final int CONSTANT_Class = 7;
	    static final int CONSTANT_Package = 20;
	    static final int CONSTANT_Module = 19;
	    static final int CONSTANT_String = 8;
	    static final int CONSTANT_Double = 6;
	    static final int CONSTANT_Fieldref = 9;
	    static final int CONSTANT_InterfaceMethodref = 11;
	    static final int CONSTANT_Methodref = 10;
	    static final int CONSTANT_InvokeDynamic = 18;
	    static final int CONSTANT_NameAndType = 12;
	    static final int CONSTANT_MethodHandle = 15;
	    static final int CONSTANT_MethodType = 16;
	    static final int CONSTANT_Float = 4;
	    static final int CONSTANT_Integer = 3;
	    static final int CONSTANT_Long = 5;

	    Index2Entry(int tag, int index1, int index2) {
		super(tag);
		this.index1 = index1;
		this.index2 = index2;
	    }

	}

	class Entry {
	    final Entry[] pool;
	    static final int CONSTANT_Utf8 = 1;
	    static final int CONSTANT_Class = 7;
	    static final int CONSTANT_Package = 20;
	    static final int CONSTANT_Module = 19;
	    static final int CONSTANT_String = 8;
	    static final int CONSTANT_Double = 6;
	    static final int CONSTANT_Fieldref = 9;
	    static final int CONSTANT_InterfaceMethodref = 11;
	    static final int CONSTANT_Methodref = 10;
	    static final int CONSTANT_InvokeDynamic = 18;
	    static final int CONSTANT_NameAndType = 12;
	    static final int CONSTANT_MethodHandle = 15;
	    static final int CONSTANT_MethodType = 16;
	    static final int CONSTANT_Float = 4;
	    static final int CONSTANT_Integer = 3;
	    static final int CONSTANT_Long = 5;

	    protected Entry(int tag) {
		this.tag = tag;
	    }

	}

    }

    class Attributes {
	private final int JAVA_MIN_SUPPORTED_VERSION = 53;
	private final int JAVA_MAX_SUPPORTED_VERSION = 55;
	private final boolean parseHashes;
	private final Supplier&lt;Set&lt;String&gt;&gt; packageFinder;
	private static final JavaLangModuleAccess JLMA = SharedSecrets.getJavaLangModuleAccess();
	private static volatile Set&lt;String&gt; predefinedNotAllowed;

	Attributes(ModuleDescriptor descriptor, ModuleTarget target, ModuleHashes recordedHashes,
		ModuleResolution moduleResolution) {
	    this.descriptor = descriptor;
	    this.target = target;
	    this.recordedHashes = recordedHashes;
	    this.moduleResolution = moduleResolution;
	}

    }

}

