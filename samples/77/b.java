import java.util.List;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.drivers.ScrollDriver;

class JSpinnerOperator extends JComponentOperator implements Timeoutable, Outputable {
    class ListSpinnerOperator extends JSpinnerOperator {
	/**
	 * Scrolls to {@code getValue().toString()} match a specified
	 * pattern.
	 *
	 * @param pattern a string pattern
	 * @param comparator a string comparision criteria.
	 */
	public void scrollToString(String pattern, StringComparator comparator) {
	    int index = findItem(pattern, comparator);
	    if (index != -1) {
		scrollToIndex(index);
	    } else {
		throw (new JemmyException("No \"" + pattern + "\" item in JSpinner", getSource()));
	    }
	}

	/**
	 * Looks for an index of an item having {@code toString()} matching
	 * a specified pattern.
	 *
	 * @param pattern a string pattern
	 * @param comparator a string comparision criteria.
	 */
	public int findItem(String pattern, StringComparator comparator) {
	    List&lt;?&gt; list = getListModel().getList();
	    for (int i = 0; i &lt; list.size(); i++) {
		if (comparator.equals(list.get(i).toString(), pattern)) {
		    return i;
		}
	    }
	    return -1;
	}

	/**
	 * Scrolls to an item having specified instance.
	 *
	 * @param index an index to scroll to.
	 */
	public void scrollToIndex(int index) {
	    scrollTo(new ListScrollAdjuster(this, index));
	}

	/**
	 * Costs spinner's model to &lt;code&gt;SpinnerListModel&lt;code&gt;.
	 *
	 * @return a spinner model.
	 */
	public SpinnerListModel getListModel() {
	    return (SpinnerListModel) getModel();
	}

    }

    private ScrollDriver driver;

    /**
     * Scrolls to reach a condition specified by {@code ScrollAdjuster}
     *
     * @param adj scrolling criteria.
     */
    public void scrollTo(final ScrollAdjuster adj) {
	produceTimeRestricted(new Action&lt;Void, Void&gt;() {
	    @Override
	    public Void launch(Void obj) {
		driver.scroll(JSpinnerOperator.this, adj);
		return null;
	    }

	    @Override
	    public String getDescription() {
		return "Scrolling";
	    }

	    @Override
	    public String toString() {
		return "JSpinnerOperator.scrollTo.Action{description = " + getDescription() + '}';
	    }
	}, "JSpinnerOperator.WholeScrollTimeout");
    }

    /**
     * Maps {@code JSpinner.getModel()} through queue
     */
    public SpinnerModel getModel() {
	return (runMapping(new MapAction&lt;SpinnerModel&gt;("getModel") {
	    @Override
	    public SpinnerModel map() {
		return ((JSpinner) getSource()).getModel();
	    }
	}));
    }

    /**
     * Checks operator's model type.
     *
     * @param oper an operator to check model
     * @param modelClass a model class.
     * @throws SpinnerModelException if an operator's model is not an instance
     * of specified class.
     */
    public static void checkModel(JSpinnerOperator oper, Class&lt;?&gt; modelClass) {
	if (!modelClass.isInstance(oper.getModel())) {
	    throw (new SpinnerModelException("JSpinner model is not a " + modelClass.getName(), oper.getSource()));
	}
    }

    class ListScrollAdjuster implements ScrollAdjuster {
	private ScrollDriver driver;

	/**
	 * Constructs a {@code ListScrollAdjuster} object.
	 *
	 * @param oper an operator to work with.
	 * @param itemIndex an item index to scroll to.
	 */
	public ListScrollAdjuster(JSpinnerOperator oper, int itemIndex) {
	    this(oper);
	    this.itemIndex = itemIndex;
	}

	private ListScrollAdjuster(JSpinnerOperator oper) {
	    checkModel(oper, SpinnerListModel.class);
	    model = (SpinnerListModel) oper.getModel();
	    elements = model.getList();
	}

    }

    class SpinnerModelException extends JemmyException {
	private ScrollDriver driver;

	/**
	 * Constructs a {@code SpinnerModelException} object.
	 *
	 * @param message error message.
	 * @param comp a spinner which model cased the exception.
	 */
	public SpinnerModelException(String message, Component comp) {
	    super(message, comp);
	}

    }

}

