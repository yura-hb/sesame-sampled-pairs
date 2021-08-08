import java.awt.Component;
import org.netbeans.jemmy.TestOut;

class WindowOperator extends ContainerOperator&lt;Window&gt; implements Outputable {
    /**
     * Waits the window to be closed.
     */
    public void waitClosed() {
	getOutput().printLine("Wait window to be closed \n    : " + getSource().toString());
	getOutput().printGolden("Wait window to be closed");
	waitState(new ComponentChooser() {
	    @Override
	    public boolean checkComponent(Component comp) {
		return !comp.isVisible();
	    }

	    @Override
	    public String getDescription() {
		return "Closed window";
	    }

	    @Override
	    public String toString() {
		return "WindowOperator.waitClosed.Action{description = " + getDescription() + '}';
	    }
	});
    }

    TestOut output;

    @Override
    public TestOut getOutput() {
	return output;
    }

}

