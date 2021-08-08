import java.awt.Adjustable;
import org.netbeans.jemmy.drivers.ScrollDriver;

class ScrollPaneOperator extends ContainerOperator&lt;ScrollPane&gt; implements Timeoutable, Outputable {
    /**
     * Scrolls pane to rectangle..
     *
     * @param comp a subcomponent defining coordinate system.
     * @param x coordinate
     * @param y coordinate
     * @param width rectangle width
     * @param height rectangle height
     * @throws TimeoutExpiredException
     */
    public void scrollToComponentRectangle(Component comp, int x, int y, int width, int height) {
	scrollTo(new ComponentRectChecker(comp, x, y, width, height, Adjustable.HORIZONTAL));
	scrollTo(new ComponentRectChecker(comp, x, y, width, height, Adjustable.VERTICAL));
    }

    private ScrollDriver driver;

    /**
     * Scrools to the position defined by a ScrollAdjuster instance.
     *
     * @param adj specifies the position.
     */
    public void scrollTo(final ScrollAdjuster adj) {
	produceTimeRestricted(new Action&lt;Void, Void&gt;() {
	    @Override
	    public Void launch(Void obj) {
		driver.scroll(ScrollPaneOperator.this, adj);
		return null;
	    }

	    @Override
	    public String getDescription() {
		return "Scrolling";
	    }

	    @Override
	    public String toString() {
		return "ScrollPaneOperator.scrollTo.Action{description = " + getDescription() + '}';
	    }
	}, "ScrollbarOperator.WholeScrollTimeout");
    }

    class ComponentRectChecker implements ScrollAdjuster {
	private ScrollDriver driver;

	public ComponentRectChecker(Component comp, int x, int y, int width, int height, int orientation) {
	    this.comp = comp;
	    this.x = x;
	    this.y = y;
	    this.width = width;
	    this.height = height;
	    this.orientation = orientation;
	}

    }

}

