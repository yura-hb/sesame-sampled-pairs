import javax.swing.JList;
import javax.swing.ListModel;
import org.netbeans.jemmy.JemmyInputException;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.drivers.MultiSelListDriver;

class JListOperator extends JComponentOperator implements Outputable {
    /**
     * Selects items by indices.
     *
     * @param indices item indices.
     */
    public void selectItems(int[] indices) {
	checkIndices(indices);
	driver.selectItems(this, indices);
	if (getVerification()) {
	    waitItemsSelection(indices, true);
	}
    }

    private MultiSelListDriver driver;
    private TestOut output;

    private void checkIndices(int[] indices) {
	for (int indice : indices) {
	    checkIndex(indice);
	}
    }

    /**
     * Waits for items to be selected.
     *
     * @param itemIndices item indices to be selected
     * @param selected Selected (true) or unselected (false).
     */
    public void waitItemsSelection(final int[] itemIndices, final boolean selected) {
	getOutput().printLine(
		"Wait items to be " + (selected ? "" : "un") + "selected in component \n    : " + toStringSource());
	getOutput().printGolden("Wait items to be " + (selected ? "" : "un") + "selected");
	waitState(new ComponentChooser() {
	    @Override
	    public boolean checkComponent(Component comp) {
		int[] indices = getSelectedIndices();
		for (int i = 0; i &lt; indices.length; i++) {
		    if (indices[i] != itemIndices[i]) {
			return false;
		    }
		}
		return true;
	    }

	    @Override
	    public String getDescription() {
		return ("Item has been " + (selected ? "" : "un") + "selected");
	    }

	    @Override
	    public String toString() {
		return "JListOperator.waitItemsSelection.ComponentChooser{description = " + getDescription() + '}';
	    }
	});
    }

    private void checkIndex(int index) {
	if (index &lt; 0 || index &gt;= getModel().getSize()) {
	    throw (new NoSuchItemException(index));
	}
    }

    @Override
    public TestOut getOutput() {
	return output;
    }

    /**
     * Maps {@code JList.getSelectedIndices()} through queue
     */
    public int[] getSelectedIndices() {
	return ((int[]) runMapping(new MapAction&lt;Object&gt;("getSelectedIndices") {
	    @Override
	    public Object map() {
		return ((JList) getSource()).getSelectedIndices();
	    }
	}));
    }

    /**
     * Maps {@code JList.getModel()} through queue
     */
    public ListModel&lt;?&gt; getModel() {
	return (runMapping(new MapAction&lt;ListModel&lt;?&gt;&gt;("getModel") {
	    @Override
	    public ListModel&lt;?&gt; map() {
		return ((JList) getSource()).getModel();
	    }
	}));
    }

    class NoSuchItemException extends JemmyInputException {
	private MultiSelListDriver driver;
	private TestOut output;

	/**
	 * Constructor.
	 *
	 * @param index an item's index
	 */
	public NoSuchItemException(int index) {
	    super("List does not contain " + index + "'th item", getSource());
	}

    }

}

