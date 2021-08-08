import java.io.*;

class RMID extends JavaVM {
    /**
     * Routine that creates an rmid that will run with or without a
     * policy file.
     */
    public static RMID createRMID() {
	return createRMID(System.out, System.err, true, true, TestLibrary.getUnusedRandomPort());
    }

    private static long inheritedChannelTimeout;
    /** rmid's logfile directory; currently must be "." */
    protected static String LOGDIR = ".";
    /** Initial log name */
    protected static String log = "log";
    /**
     * Test port for rmid.
     *
     * May initially be 0, which means that the child rmid process will choose
     * an ephemeral port and report it back to the parent process. This field
     * will then be set to the child rmid's ephemeral port value.
     */
    private volatile int port;
    private static final long TIMEOUT_BASE = 240_000L;

    public static RMID createRMID(OutputStream out, OutputStream err, boolean debugExec, boolean includePortArg,
	    int port) {
	return createRMIDWithOptions(out, err, debugExec, includePortArg, port, "");
    }

    /**
     * Create a RMID on a specified port capturing stdout and stderr
     * with additional command line options and whether to print out
     * debugging information that is used for spawning activation groups.
     *
     * @param out the OutputStream where the normal output of the
     *            rmid subprocess goes
     * @param err the OutputStream where the error output of the
     *            rmid subprocess goes
     * @param debugExec whether to print out debugging information
     * @param includePortArg whether to include port argument
     * @param port the port on which rmid accepts requests
     * @param additionalOptions additional command line options
     * @return a RMID instance
     */
    public static RMID createRMIDWithOptions(OutputStream out, OutputStream err, boolean debugExec,
	    boolean includePortArg, int port, String additionalOptions) {
	String options = makeOptions(port, debugExec, false);
	options += " " + additionalOptions;
	String args = makeArgs(includePortArg, port);
	RMID rmid = new RMID("sun.rmi.server.Activation", options, args, out, err, port);
	rmid.setPolicyFile(TestParams.defaultRmidPolicy);

	return rmid;
    }

    /** make test options and arguments */
    private static String makeOptions(int port, boolean debugExec, boolean enableSelectorProvider) {

	String options = " -Dsun.rmi.server.activation.debugExec=" + debugExec;
	// +
	//" -Djava.compiler= ";

	// if test params set, want to propagate them
	if (!TestParams.testSrc.equals("")) {
	    options += " -Dtest.src=" + TestParams.testSrc + " ";
	}
	//if (!TestParams.testClasses.equals("")) {
	//    options += " -Dtest.classes=" + TestParams.testClasses + " ";
	//}
	options += " -Dtest.classes=" + TestParams.testClasses //;
		+ " -Djava.rmi.server.logLevel=v ";

	// +
	// " -Djava.security.debug=all ";

	// Set execTimeout to 60 sec (default is 30 sec)
	// to avoid spurious timeouts on slow machines.
	options += " -Dsun.rmi.activation.execTimeout=60000";

	// It's important to set handshakeTimeout to small value, for example
	// 5 sec (default is 60 sec) to avoid wasting too much time when
	// calling lookupSystem(port) in restart(), because
	//   1. If use default value of this option, it will take about 2 minutes
	//     to finish lookupSystem(port) in 2 loops in restart();
	//   2. If set this option as 5 sec then lookupSystem(port) will return
	//     very quickly.
	options += " -Dsun.rmi.transport.tcp.handshakeTimeout=5000";

	if (port == 0 || enableSelectorProvider) {
	    // Ephemeral port, so have the rmid child process create the
	    // server socket channel and report its port number, over stdin.
	    options += " -classpath " + TestParams.testClassPath;
	    options += " --add-exports=java.base/sun.nio.ch=ALL-UNNAMED";
	    options += " -Djava.nio.channels.spi.SelectorProvider=RMIDSelectorProvider";
	    options += " -Dtest.java.rmi.testlibrary.RMIDSelectorProvider.port=" + port;
	    options += " -Dtest.java.rmi.testlibrary.RMIDSelectorProvider.timeout=" + inheritedChannelTimeout;

	    // Disable redirection of System.err to /tmp
	    options += " -Dsun.rmi.server.activation.disableErrRedirect=true";
	}

	return options;
    }

    private static String makeArgs(boolean includePortArg, int port) {
	// getAbsolutePath requires permission to read user.dir
	String args = " -log " + (new File(LOGDIR, log)).getAbsolutePath();

	// 0 = ephemeral port, do not include an explicit port number
	if (includePortArg && port != 0) {
	    args += " -port " + port;
	}

	// +
	//      " -C-Djava.compiler= ";

	// if test params set, want to propagate them
	if (!TestParams.testSrc.equals("")) {
	    args += " -C-Dtest.src=" + TestParams.testSrc;
	}
	if (!TestParams.testClasses.equals("")) {
	    args += " -C-Dtest.classes=" + TestParams.testClasses;
	}

	if (!TestParams.testJavaOpts.equals("")) {
	    for (String a : TestParams.testJavaOpts.split(" +")) {
		args += " -C" + a;
	    }
	}

	if (!TestParams.testVmOpts.equals("")) {
	    for (String a : TestParams.testVmOpts.split(" +")) {
		args += " -C" + a;
	    }
	}

	args += " -C-Djava.rmi.server.useCodebaseOnly=false ";

	args += " " + getCodeCoverageArgs();
	return args;
    }

    /**
     * Private constructor. RMID instances should be created
     * using the static factory methods.
     */
    private RMID(String classname, String options, String args, OutputStream out, OutputStream err, int port) {
	super(classname, options, args, out, err);
	this.port = port;
	long waitTime = (long) (TIMEOUT_BASE * TestLibrary.getTimeoutFactor());
	inheritedChannelTimeout = (long) (waitTime * 0.8);
    }

    /**
     * This method is used for adding arguments to rmid (not its VM)
     * for passing as VM options to its child group VMs.
     * Returns the extra command line arguments required
     * to turn on jcov code coverage analysis for rmid child VMs.
     */
    protected static String getCodeCoverageArgs() {
	return TestLibrary.getExtraProperty("rmid.jcov.args", "");
    }

}

