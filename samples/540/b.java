import com.sun.tools.attach.VirtualMachine;
import jdk.test.lib.management.DynamicVMOption;
import jdk.test.lib.process.ProcessTools;
import sun.tools.attach.HotSpotVirtualMachine;
import static optionsvalidation.JVMOptionsUtils.failedMessage;

abstract class JVMOption {
    /**
     * Testing writeable option using attach method
     *
     * @return number of failed tests
     * @throws Exception if an error occurred while attaching to the target JVM
     */
    public int testAttach() throws Exception {
	DynamicVMOption option = new DynamicVMOption(name);
	int failedTests = 0;
	String origValue;

	if (option.isWriteable()) {

	    System.out.println("Testing " + name + " option dynamically via attach");

	    origValue = option.getValue();

	    HotSpotVirtualMachine vm = (HotSpotVirtualMachine) VirtualMachine
		    .attach(String.valueOf(ProcessTools.getProcessId()));

	    for (String value : getValidValues()) {
		if (!setFlagAttach(vm, name, value)) {
		    failedMessage(String.format("Option %s: Can not change option to valid value \"%s\" via attach",
			    name, value));
		    failedTests++;
		}
	    }

	    for (String value : getInvalidValues()) {
		if (setFlagAttach(vm, name, value)) {
		    failedMessage(
			    String.format("Option %s: Option changed to invalid value \"%s\" via attach", name, value));
		    failedTests++;
		}
	    }

	    vm.detach();

	    option.setValue(origValue);
	}

	return failedTests;
    }

    /**
     * Name of the tested parameter
     */
    protected String name;

    /**
     * Return list of strings with valid option values which used for testing
     * using jcmd, attach and etc.
     *
     * @return list of strings which contain valid values for option
     */
    protected abstract List&lt;String&gt; getValidValues();

    private boolean setFlagAttach(HotSpotVirtualMachine vm, String flagName, String flagValue) throws Exception {
	boolean result;

	try {
	    vm.setFlag(flagName, flagValue);
	    result = true;
	} catch (AttachOperationFailedException e) {
	    result = false;
	}

	return result;
    }

    /**
     * Return list of strings with invalid option values which used for testing
     * using jcmd, attach and etc.
     *
     * @return list of strings which contain invalid values for option
     */
    protected abstract List&lt;String&gt; getInvalidValues();

}

