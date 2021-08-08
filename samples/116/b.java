import javax.swing.JTree;
import org.netbeans.jemmy.TestOut;

class JTreeOperator extends JComponentOperator implements Timeoutable, Outputable {
    /**
     * Waits row to be expanded.
     *
     * @param row a row index to wait expanded.
     */
    public void waitExpanded(final int row) {
	getOutput().printLine(
		"Wait " + Integer.toString(row) + "'th row to be expanded in component \n    : " + toStringSource());
	getOutput().printGolden("Wait " + Integer.toString(row) + "'th row to be expanded");
	waitState(new ComponentChooser() {
	    @Override
	    public boolean checkComponent(Component comp) {
		return isExpanded(row);
	    }

	    @Override
	    public String getDescription() {
		return "Has " + Integer.toString(row) + "'th row expanded";
	    }

	    @Override
	    public String toString() {
		return "JTreeOperator.waitExpanded.ComponentChooser{description = " + getDescription() + '}';
	    }
	});
    }

    private TestOut output;

    @Override
    public TestOut getOutput() {
	return output;
    }

    /**
     * Maps {@code JTree.isExpanded(int)} through queue
     */
    public boolean isExpanded(final int i) {
	return (runMapping(new MapBooleanAction("isExpanded") {
	    @Override
	    public boolean map() {
		return ((JTree) getSource()).isExpanded(i);
	    }
	}));
    }

}

