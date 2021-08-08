import junit.framework.*;

class TestRunner extends BaseTestRunner {
    /**
     * Runs a single test and collects its results.
     * This method can be used to start a test run
     * from your program.
     * &lt;pre&gt;
     * public static void main (String[] args) {
     *     test.textui.TestRunner.run(suite());
     * }
     * &lt;/pre&gt;
     */
    static public TestResult run(Test test) {
	TestRunner runner = new TestRunner();
	return runner.doRun(test);
    }

    private ResultPrinter fPrinter;

    /**
     * Constructs a TestRunner.
     */
    public TestRunner() {
	this(System.out);
    }

    public TestResult doRun(Test test) {
	return doRun(test, false);
    }

    /**
     * Constructs a TestRunner using the given stream for all the output
     */
    public TestRunner(PrintStream writer) {
	this(new ResultPrinter(writer));
    }

    public TestResult doRun(Test suite, boolean wait) {
	TestResult result = createTestResult();
	result.addListener(fPrinter);
	long startTime = System.currentTimeMillis();
	suite.run(result);
	long endTime = System.currentTimeMillis();
	long runTime = endTime - startTime;
	fPrinter.print(result, runTime);

	pause(wait);
	return result;
    }

    /**
     * Constructs a TestRunner using the given ResultPrinter all the output
     */
    public TestRunner(ResultPrinter printer) {
	fPrinter = printer;
    }

    /**
     * Creates the TestResult to be used for the test run.
     */
    protected TestResult createTestResult() {
	return new TestResult();
    }

    protected void pause(boolean wait) {
	if (!wait)
	    return;
	fPrinter.printWaitPrompt();
	try {
	    System.in.read();
	} catch (Exception e) {
	}
    }

}

