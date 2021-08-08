import java.io.*;
import java.util.StringTokenizer;

class CheckAnnotations extends Activatable implements MyRMI, Runnable {
    /**
     * check to make sure that the output from a spawned vm is
     * formatted/annotated properly.
     */
    public static void checkAnnotations(int iteration) throws IOException {
	try {
	    Thread.sleep((long) (5000 * TIME_FACTOR));
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	}

	final String FAIL_MSG = "Test failed: output improperly annotated.";
	final String OUT = "outABC";
	final String ERR = "errXYZ";
	/**
	 * cause the spawned vm to generate output that will
	 * be checked for proper annotation.  printOut is
	 * actually being called on an activated implementation.
	 */
	myRMI.printOut(OUT + iteration);
	myRMI.printErr(ERR + iteration);
	myRMI.printOut(OUT + iteration);
	myRMI.printErr(ERR + iteration);

	/* we have to wait for output to filter down
	 * from children so we can read it before we
	 * kill rmid.
	 */

	String outString = null;
	String errString = null;

	for (int i = 0; i &lt; 5; i++) {
	    // have to give output from rmid time to trickle down to
	    // this process
	    try {
		Thread.sleep((long) (4000 * TIME_FACTOR));
	    } catch (InterruptedException e) {
	    }

	    outString = rmidOut.toString();
	    errString = rmidErr.toString();

	    if ((!outString.equals("")) && (!errString.equals(""))) {
		System.err.println("obtained annotations");
		break;
	    }
	    System.err.println("rmid output not yet received, retrying...");
	}

	rmidOut.reset();
	rmidErr.reset();

	// only test when we are annotating..., first run does not annotate
	if (iteration &gt;= 0) {
	    System.err.println("Checking annotations...");
	    System.err.println(outString);
	    System.err.println(errString);

	    StringTokenizer stOut = new StringTokenizer(outString, ":");
	    StringTokenizer stErr = new StringTokenizer(errString, ":");

	    String execErr = null;
	    String execOut = null;
	    String destOut = null;
	    String destErr = null;
	    String outTmp = null;
	    String errTmp = null;

	    while (stOut.hasMoreTokens()) {
		execOut = outTmp;
		outTmp = destOut;
		destOut = stOut.nextToken();
	    }
	    while (stErr.hasMoreTokens()) {
		execErr = errTmp;
		errTmp = destErr;
		destErr = stErr.nextToken();
	    }

	    if ((execErr == null) || (errTmp == null) || (destErr == null)) {
		TestLibrary.bomb(FAIL_MSG);
	    }
	    if ((execOut == null) || (outTmp == null) || (destOut == null)) {
		TestLibrary.bomb(FAIL_MSG);
	    }

	    // just make sure that last two strings are what we expect.
	    if (!execOut.equals("ExecGroup-" + iteration)
		    || !(new String(destOut.substring(0, OUT.length() + 1)).equals(OUT + iteration))
		    || !(execErr.equals("ExecGroup-" + iteration))
		    || !(new String(destErr.substring(0, ERR.length() + 1)).equals(ERR + iteration))) {
		TestLibrary.bomb(FAIL_MSG);
	    }
	}
    }

    private static final double TIME_FACTOR = TestLibrary.getTimeoutFactor();
    private static MyRMI myRMI = null;
    private static ByteArrayOutputStream rmidOut = new ByteArrayOutputStream();
    private static ByteArrayOutputStream rmidErr = new ByteArrayOutputStream();

}

