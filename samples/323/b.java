import java.awt.Rectangle;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyInputException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.util.EmptyVisualizer;

class JTreeOperator extends JComponentOperator implements Timeoutable, Outputable {
    /**
     * Selects the path.
     *
     * @param path a path to select.
     */
    public void selectPath(final TreePath path) {
	if (path != null) {
	    output.printLine("Selecting \"" + path.toString() + "\" path");
	    output.printGolden("Selecting path");
	    scrollToPath(path);
	    getQueueTool().invokeSmoothly(new QueueTool.QueueAction&lt;Void&gt;("Path selecting") {
		@Override
		public Void launch() {
		    driver.selectItem(JTreeOperator.this, getRowForPath(path));
		    return null;
		}
	    });
	    if (getVerification()) {
		waitSelected(path);
	    }
	} else {
	    throw (new NoSuchPathException());
	}
    }

    private TestOut output;
    private TreeDriver driver;

    /**
     * Scrolls to a path if the tree is on a JScrollPane component.
     *
     * @param path a tree path to scroll to.
     */
    public void scrollToPath(TreePath path) {
	if (path != null) {
	    output.printTrace("Scroll JTree to path \"" + path.toString() + "\"\n    : " + toStringSource());
	    output.printGolden("Scroll JTree to path \"" + path.toString() + "\"");
	    makeComponentVisible();
	    //try to find JScrollPane under.
	    JScrollPane scroll = (JScrollPane) getContainer(
		    new JScrollPaneOperator.JScrollPaneFinder(ComponentSearcher.getTrueChooser("JScrollPane")));
	    if (scroll == null) {
		return;
	    }
	    JScrollPaneOperator scroller = new JScrollPaneOperator(scroll);
	    scroller.copyEnvironment(this);
	    scroller.setVisualizer(new EmptyVisualizer());
	    Rectangle rect = getPathBounds(path);
	    if (rect != null) {
		scroller.scrollToComponentRectangle(getSource(), (int) rect.getX(), (int) rect.getY(),
			(int) rect.getWidth(), (int) rect.getHeight());
	    } else {
		throw (new NoSuchPathException(path));
	    }
	} else {
	    throw (new NoSuchPathException());
	}
    }

    /**
     * Maps {@code JTree.getRowForPath(TreePath)} through queue
     */
    public int getRowForPath(final TreePath treePath) {
	return (runMapping(new MapIntegerAction("getRowForPath") {
	    @Override
	    public int map() {
		return ((JTree) getSource()).getRowForPath(treePath);
	    }
	}));
    }

    /**
     * Waits path to be selected.
     *
     * @param path a tree path to be selected.
     */
    public void waitSelected(final TreePath path) {
	waitSelected(new TreePath[] { path });
    }

    /**
     * Maps {@code JTree.getPathBounds(TreePath)} through queue
     */
    public Rectangle getPathBounds(final TreePath treePath) {
	return (runMapping(new MapAction&lt;Rectangle&gt;("getPathBounds") {
	    @Override
	    public Rectangle map() {
		return ((JTree) getSource()).getPathBounds(treePath);
	    }
	}));
    }

    /**
     * Waits some paths to be selected.
     *
     * @param paths an array of paths to be selected.
     */
    public void waitSelected(final TreePath[] paths) {
	getOutput().printLine("Wait right selection in component \n    : " + toStringSource());
	getOutput().printGolden("Wait right selection");
	waitState(new ComponentChooser() {
	    @Override
	    public boolean checkComponent(Component comp) {
		TreePath[] rpaths = getSelectionModel().getSelectionPaths();
		if (rpaths != null) {
		    for (int i = 0; i &lt; rpaths.length; i++) {
			if (!rpaths[i].equals(paths[i])) {
			    return false;
			}
		    }
		    return true;
		} else {
		    return false;
		}
	    }

	    @Override
	    public String getDescription() {
		return "Has right selection";
	    }

	    @Override
	    public String toString() {
		return "JTreeOperator.waitSelected.ComponentChooser{description = " + getDescription() + '}';
	    }
	});
    }

    @Override
    public TestOut getOutput() {
	return output;
    }

    /**
     * Maps {@code JTree.getSelectionModel()} through queue
     */
    public TreeSelectionModel getSelectionModel() {
	return (runMapping(new MapAction&lt;TreeSelectionModel&gt;("getSelectionModel") {
	    @Override
	    public TreeSelectionModel map() {
		return ((JTree) getSource()).getSelectionModel();
	    }
	}));
    }

    class NoSuchPathException extends JemmyInputException {
	private TestOut output;
	private TreeDriver driver;

	/**
	 * Constructor.
	 */
	public NoSuchPathException() {
	    super("Unknown/null/invalid tree path.", null);
	}

	/**
	 * Constructor.
	 *
	 * @param path a nonexistent path.
	 */
	public NoSuchPathException(TreePath path) {
	    super("No such path as \"" + path.toString() + "\"", getSource());
	}

    }

}

