import org.netbeans.jemmy.TestOut;

class JTextComponentOperator extends JComponentOperator implements Timeoutable, Outputable {
    /**
     * Waits for certain text.
     *
     * @param text Text to be compared by getComparator() comparator.
     */
    public void waitText(String text) {
	getOutput().printLine("Wait \"" + text + "\" text in component \n    : " + toStringSource());
	getOutput().printGolden("Wait \"" + text + "\" text");
	waitState(new JTextComponentByTextFinder(text, getComparator()));
    }

    private TestOut output;

    @Override
    public TestOut getOutput() {
	return output;
    }

    class JTextComponentByTextFinder implements ComponentChooser {
	private TestOut output;

	/**
	 * Constructs JTextComponentByTextFinder.
	 *
	 * @param lb a text pattern
	 * @param comparator specifies string comparision algorithm.
	 */
	public JTextComponentByTextFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}

    }

}

