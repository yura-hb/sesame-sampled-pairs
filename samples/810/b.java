import jdk.internal.vm.compiler.collections.EconomicMap;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.code.RegisterArray;
import jdk.vm.ci.code.RegisterConfig;
import jdk.vm.ci.meta.PlatformKind;

class RegisterAllocationConfig {
    /**
     * Gets the set of registers that can be used by the register allocator for a value of a
     * particular kind.
     */
    public AllocatableRegisters getAllocatableRegisters(PlatformKind kind) {
	PlatformKind.Key key = kind.getKey();
	if (categorized.containsKey(key)) {
	    AllocatableRegisters val = categorized.get(key);
	    return val;
	}
	AllocatableRegisters ret = createAllocatableRegisters(
		registerConfig.filterAllocatableRegisters(kind, getAllocatableRegisters()));
	categorized.put(key, ret);
	return ret;
    }

    private final EconomicMap&lt;PlatformKind.Key, AllocatableRegisters&gt; categorized = EconomicMap
	    .create(Equivalence.DEFAULT);
    protected final RegisterConfig registerConfig;
    private RegisterArray cachedRegisters;
    private final String[] allocationRestrictedTo;

    /**
     * Gets the set of registers that can be used by the register allocator.
     */
    public RegisterArray getAllocatableRegisters() {
	if (cachedRegisters == null) {
	    cachedRegisters = initAllocatable(registerConfig.getAllocatableRegisters());
	}
	assert cachedRegisters != null;
	return cachedRegisters;
    }

    protected AllocatableRegisters createAllocatableRegisters(RegisterArray registers) {
	int min = Integer.MAX_VALUE;
	int max = Integer.MIN_VALUE;
	for (Register reg : registers) {
	    int number = reg.number;
	    if (number &lt; min) {
		min = number;
	    }
	    if (number &gt; max) {
		max = number;
	    }
	}
	assert min &lt; max;
	return new AllocatableRegisters(registers, min, max);

    }

    protected RegisterArray initAllocatable(RegisterArray registers) {
	if (allocationRestrictedTo != null) {
	    Register[] regs = new Register[allocationRestrictedTo.length];
	    for (int i = 0; i &lt; allocationRestrictedTo.length; i++) {
		regs[i] = findRegister(allocationRestrictedTo[i], registers);
	    }
	    return new RegisterArray(regs);
	}

	return registers;
    }

    private static Register findRegister(String name, RegisterArray all) {
	for (Register reg : all) {
	    if (reg.name.equals(name)) {
		return reg;
	    }
	}
	throw new IllegalArgumentException("register " + name + " is not allocatable");
    }

    class AllocatableRegisters {
	private final EconomicMap&lt;PlatformKind.Key, AllocatableRegisters&gt; categorized = EconomicMap
		.create(Equivalence.DEFAULT);
	protected final RegisterConfig registerConfig;
	private RegisterArray cachedRegisters;
	private final String[] allocationRestrictedTo;

	public AllocatableRegisters(RegisterArray allocatableRegisters, int minRegisterNumber, int maxRegisterNumber) {
	    this.allocatableRegisters = allocatableRegisters.toArray();
	    this.minRegisterNumber = minRegisterNumber;
	    this.maxRegisterNumber = maxRegisterNumber;
	    assert verify(allocatableRegisters, minRegisterNumber, maxRegisterNumber);
	}

	private static boolean verify(RegisterArray allocatableRegisters, int minRegisterNumber,
		int maxRegisterNumber) {
	    int min = Integer.MAX_VALUE;
	    int max = Integer.MIN_VALUE;
	    for (Register reg : allocatableRegisters) {
		int number = reg.number;
		if (number &lt; min) {
		    min = number;
		}
		if (number &gt; max) {
		    max = number;
		}
	    }
	    assert minRegisterNumber == min;
	    assert maxRegisterNumber == max;
	    return true;
	}

    }

}

