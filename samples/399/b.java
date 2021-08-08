import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;
import sun.jvm.hotspot.runtime.*;

class VM {
    /** This could be used by a reflective runtime system */
    public static void initialize(TypeDataBase db, boolean isBigEndian) {
	if (soleInstance != null) {
	    throw new RuntimeException("Attempt to initialize VM twice");
	}
	soleInstance = new VM(db, null, isBigEndian);
	for (Iterator iter = vmInitializedObservers.iterator(); iter.hasNext();) {
	    ((Observer) iter.next()).update(null, null);
	}
    }

    private static VM soleInstance;
    private static List vmInitializedObservers = new ArrayList();
    private TypeDataBase db;
    /** This is only present if in a debugging system */
    private JVMDebugger debugger;
    private boolean isBigEndian;
    private long logAddressSize;
    private String vmRelease;
    private String vmInternalInfo;
    private int reserveForAllocationPrefetch;
    private long stackBias;
    /** These constants come from globalDefinitions.hpp */
    private int invocationEntryBCI;
    /** Flag indicating if JVMTI support is included in the build */
    private boolean isJvmtiSupported;
    /** Flags indicating whether we are attached to a core, C1, or C2 build */
    private boolean usingClientCompiler;
    private boolean usingServerCompiler;
    /** alignment constants */
    private boolean isLP64;
    private int bytesPerLong;
    private int bytesPerWord;
    private int heapWordSize;
    private int oopSize;
    private final int IndexSetSize;
    private static Type intType;
    private static Type uintType;
    private static Type intxType;
    private static Type uintxType;
    private static Type sizetType;
    private static CIntegerType boolType;
    private int minObjAlignmentInBytes;
    private int logMinObjAlignmentInBytes;
    private int heapOopSize;
    private int klassPtrSize;
    private static final Properties saProps;
    private int objectAlignmentInBytes;
    private Boolean compressedOopsEnabled;
    private Boolean compressedKlassPointersEnabled;
    private Map flagsMap;
    private Flag[] commandLineFlags;

    private VM(TypeDataBase db, JVMDebugger debugger, boolean isBigEndian) {
	this.db = db;
	this.debugger = debugger;
	this.isBigEndian = isBigEndian;

	// Note that we don't construct universe, heap, threads,
	// interpreter, or stubRoutines here (any more).  The current
	// initialization mechanisms require that the VM be completely set
	// up (i.e., out of its constructor, with soleInstance assigned)
	// before their static initializers are run.

	if (db.getAddressSize() == 4) {
	    logAddressSize = 2;
	} else if (db.getAddressSize() == 8) {
	    logAddressSize = 3;
	} else {
	    throw new RuntimeException("Address size " + db.getAddressSize() + " not yet supported");
	}

	// read VM version info
	try {
	    Type vmVersion = db.lookupType("Abstract_VM_Version");
	    Address releaseAddr = vmVersion.getAddressField("_s_vm_release").getValue();
	    vmRelease = CStringUtilities.getString(releaseAddr);
	    Address vmInternalInfoAddr = vmVersion.getAddressField("_s_internal_vm_info_string").getValue();
	    vmInternalInfo = CStringUtilities.getString(vmInternalInfoAddr);

	    Type threadLocalAllocBuffer = db.lookupType("ThreadLocalAllocBuffer");
	    CIntegerType intType = (CIntegerType) db.lookupType("int");
	    CIntegerField reserveForAllocationPrefetchField = threadLocalAllocBuffer
		    .getCIntegerField("_reserve_for_allocation_prefetch");
	    reserveForAllocationPrefetch = (int) reserveForAllocationPrefetchField.getCInteger(intType);
	} catch (Exception exp) {
	    throw new RuntimeException("can't determine target's VM version : " + exp.getMessage());
	}

	checkVMVersion(vmRelease);

	stackBias = db.lookupIntConstant("STACK_BIAS").intValue();
	invocationEntryBCI = db.lookupIntConstant("InvocationEntryBci").intValue();

	// We infer the presence of JVMTI from the presence of the InstanceKlass::_breakpoints field.
	{
	    Type type = db.lookupType("InstanceKlass");
	    if (type.getField("_breakpoints", false, false) == null) {
		isJvmtiSupported = false;
	    } else {
		isJvmtiSupported = true;
	    }
	}

	// We infer the presence of C1 or C2 from a couple of fields we
	// already have present in the type database
	{
	    Type type = db.lookupType("Method");
	    if (type.getField("_from_compiled_entry", false, false) == null) {
		// Neither C1 nor C2 is present
		usingClientCompiler = false;
		usingServerCompiler = false;
	    } else {
		// Determine whether C2 is present
		if (db.lookupType("Matcher", false) != null) {
		    usingServerCompiler = true;
		} else {
		    usingClientCompiler = true;
		}
	    }
	}

	if (debugger != null) {
	    isLP64 = debugger.getMachineDescription().isLP64();
	}
	bytesPerLong = db.lookupIntConstant("BytesPerLong").intValue();
	bytesPerWord = db.lookupIntConstant("BytesPerWord").intValue();
	heapWordSize = db.lookupIntConstant("HeapWordSize").intValue();
	oopSize = db.lookupIntConstant("oopSize").intValue();
	IndexSetSize = db.lookupIntConstant("CompactibleFreeListSpace::IndexSetSize").intValue();

	intType = db.lookupType("int");
	uintType = db.lookupType("uint");
	intxType = db.lookupType("intx");
	uintxType = db.lookupType("uintx");
	sizetType = db.lookupType("size_t");
	boolType = (CIntegerType) db.lookupType("bool");

	minObjAlignmentInBytes = getObjectAlignmentInBytes();
	if (minObjAlignmentInBytes == 8) {
	    logMinObjAlignmentInBytes = 3;
	} else if (minObjAlignmentInBytes == 16) {
	    logMinObjAlignmentInBytes = 4;
	} else {
	    throw new RuntimeException("Object alignment " + minObjAlignmentInBytes + " not yet supported");
	}

	if (isCompressedOopsEnabled()) {
	    // Size info for oops within java objects is fixed
	    heapOopSize = (int) getIntSize();
	} else {
	    heapOopSize = (int) getOopSize();
	}

	if (isCompressedKlassPointersEnabled()) {
	    klassPtrSize = (int) getIntSize();
	} else {
	    klassPtrSize = (int) getOopSize(); // same as an oop
	}
    }

    private static void checkVMVersion(String vmRelease) {
	if (System.getProperty("sun.jvm.hotspot.runtime.VM.disableVersionCheck") == null) {
	    // read sa build version.
	    String versionProp = "sun.jvm.hotspot.runtime.VM.saBuildVersion";
	    String saVersion = saProps.getProperty(versionProp);
	    if (saVersion == null)
		throw new RuntimeException("Missing property " + versionProp);

	    // Strip nonproduct VM version substring (note: saVersion doesn't have it).
	    String vmVersion = vmRelease.replaceAll("(-fastdebug)|(-debug)|(-jvmg)|(-optimized)|(-profiled)", "");

	    if (saVersion.equals(vmVersion)) {
		// Exact match
		return;
	    }
	    if (saVersion.indexOf('-') == saVersion.lastIndexOf('-')
		    && vmVersion.indexOf('-') == vmVersion.lastIndexOf('-')) {
		// Throw exception if different release versions:
		// &lt;major&gt;.&lt;minor&gt;-b&lt;n&gt;
		throw new VMVersionMismatchException(saVersion, vmRelease);
	    } else {
		// Otherwise print warning to allow mismatch not release versions
		// during development.
		System.err.println("WARNING: Hotspot VM version " + vmRelease + " does not match with SA version "
			+ saVersion + "." + " You may see unexpected results. ");
	    }
	} else {
	    System.err.println("WARNING: You have disabled SA and VM version check. You may be "
		    + "using incompatible version of SA and you may see unexpected " + "results.");
	}
    }

    public int getObjectAlignmentInBytes() {
	if (objectAlignmentInBytes == 0) {
	    Flag flag = getCommandLineFlag("ObjectAlignmentInBytes");
	    objectAlignmentInBytes = (flag == null) ? 8 : (int) flag.getIntx();
	}
	return objectAlignmentInBytes;
    }

    public boolean isCompressedOopsEnabled() {
	if (compressedOopsEnabled == null) {
	    Flag flag = getCommandLineFlag("UseCompressedOops");
	    compressedOopsEnabled = (flag == null) ? Boolean.FALSE : (flag.getBool() ? Boolean.TRUE : Boolean.FALSE);
	}
	return compressedOopsEnabled.booleanValue();
    }

    public long getIntSize() {
	return db.getJIntType().getSize();
    }

    public long getOopSize() {
	return oopSize;
    }

    public boolean isCompressedKlassPointersEnabled() {
	if (compressedKlassPointersEnabled == null) {
	    Flag flag = getCommandLineFlag("UseCompressedClassPointers");
	    compressedKlassPointersEnabled = (flag == null) ? Boolean.FALSE
		    : (flag.getBool() ? Boolean.TRUE : Boolean.FALSE);
	}
	return compressedKlassPointersEnabled.booleanValue();
    }

    public Flag getCommandLineFlag(String name) {
	if (flagsMap == null) {
	    flagsMap = new HashMap();
	    Flag[] flags = getCommandLineFlags();
	    for (int i = 0; i &lt; flags.length; i++) {
		flagsMap.put(flags[i].getName(), flags[i]);
	    }
	}
	return (Flag) flagsMap.get(name);
    }

    public Flag[] getCommandLineFlags() {
	if (commandLineFlags == null) {
	    readCommandLineFlags();
	}

	return commandLineFlags;
    }

    private void readCommandLineFlags() {
	// get command line flags
	TypeDataBase db = getTypeDataBase();
	Type flagType = db.lookupType("JVMFlag");
	int numFlags = (int) flagType.getCIntegerField("numFlags").getValue();
	// NOTE: last flag contains null values.
	commandLineFlags = new Flag[numFlags - 1];

	Address flagAddr = flagType.getAddressField("flags").getValue();

	AddressField typeFld = flagType.getAddressField("_type");
	AddressField nameFld = flagType.getAddressField("_name");
	AddressField addrFld = flagType.getAddressField("_addr");
	CIntField flagsFld = new CIntField(flagType.getCIntegerField("_flags"), 0);

	long flagSize = flagType.getSize(); // sizeof(Flag)

	// NOTE: last flag contains null values.
	for (int f = 0; f &lt; numFlags - 1; f++) {
	    String type = CStringUtilities.getString(typeFld.getValue(flagAddr));
	    String name = CStringUtilities.getString(nameFld.getValue(flagAddr));
	    Address addr = addrFld.getValue(flagAddr);
	    int flags = (int) flagsFld.getValue(flagAddr);
	    commandLineFlags[f] = new Flag(type, name, addr, flags);
	    flagAddr = flagAddr.addOffsetTo(flagSize);
	}

	// sort flags by name
	Arrays.sort(commandLineFlags, new Comparator() {
	    public int compare(Object o1, Object o2) {
		Flag f1 = (Flag) o1;
		Flag f2 = (Flag) o2;
		return f1.getName().compareTo(f2.getName());
	    }
	});
    }

    public TypeDataBase getTypeDataBase() {
	return db;
    }

    class Flag {
	private static VM soleInstance;
	private static List vmInitializedObservers = new ArrayList();
	private TypeDataBase db;
	/** This is only present if in a debugging system */
	private JVMDebugger debugger;
	private boolean isBigEndian;
	private long logAddressSize;
	private String vmRelease;
	private String vmInternalInfo;
	private int reserveForAllocationPrefetch;
	private long stackBias;
	/** These constants come from globalDefinitions.hpp */
	private int invocationEntryBCI;
	/** Flag indicating if JVMTI support is included in the build */
	private boolean isJvmtiSupported;
	/** Flags indicating whether we are attached to a core, C1, or C2 build */
	private boolean usingClientCompiler;
	private boolean usingServerCompiler;
	/** alignment constants */
	private boolean isLP64;
	private int bytesPerLong;
	private int bytesPerWord;
	private int heapWordSize;
	private int oopSize;
	private final int IndexSetSize;
	private static Type intType;
	private static Type uintType;
	private static Type intxType;
	private static Type uintxType;
	private static Type sizetType;
	private static CIntegerType boolType;
	private int minObjAlignmentInBytes;
	private int logMinObjAlignmentInBytes;
	private int heapOopSize;
	private int klassPtrSize;
	private static final Properties saProps;
	private int objectAlignmentInBytes;
	private Boolean compressedOopsEnabled;
	private Boolean compressedKlassPointersEnabled;
	private Map flagsMap;
	private Flag[] commandLineFlags;

	public long getIntx() {
	    if (Assert.ASSERTS_ENABLED) {
		Assert.that(isIntx(), "not an intx flag!");
	    }
	    return addr.getCIntegerAt(0, intxType.getSize(), false);
	}

	public boolean getBool() {
	    if (Assert.ASSERTS_ENABLED) {
		Assert.that(isBool(), "not a bool flag!");
	    }
	    return addr.getCIntegerAt(0, boolType.getSize(), boolType.isUnsigned()) != 0;
	}

	public String getName() {
	    return name;
	}

	public boolean isIntx() {
	    return type.equals("intx");
	}

	public boolean isBool() {
	    return type.equals("bool");
	}

	private Flag(String type, String name, Address addr, int flags) {
	    this.type = type;
	    this.name = name;
	    this.addr = addr;
	    this.flags = flags;
	}

    }

}

