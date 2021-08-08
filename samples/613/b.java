import javax.swing.*;
import java.awt.*;

class BasicTabbedPaneUI extends TabbedPaneUI implements SwingConstants {
    class TabbedPaneLayout implements LayoutManager {
	/**
	 * Pads the tab run.
	 * @param tabPlacement the tab placement
	 * @param start the start
	 * @param end the end
	 * @param max the max
	 */
	protected void padTabRun(int tabPlacement, int start, int end, int max) {
	    Rectangle lastRect = rects[end];
	    if (tabPlacement == TOP || tabPlacement == BOTTOM) {
		int runWidth = (lastRect.x + lastRect.width) - rects[start].x;
		int deltaWidth = max - (lastRect.x + lastRect.width);
		float factor = (float) deltaWidth / (float) runWidth;

		for (int j = start; j &lt;= end; j++) {
		    Rectangle pastRect = rects[j];
		    if (j &gt; start) {
			pastRect.x = rects[j - 1].x + rects[j - 1].width;
		    }
		    pastRect.width += Math.round((float) pastRect.width * factor);
		}
		lastRect.width = max - lastRect.x;
	    } else {
		int runHeight = (lastRect.y + lastRect.height) - rects[start].y;
		int deltaHeight = max - (lastRect.y + lastRect.height);
		float factor = (float) deltaHeight / (float) runHeight;

		for (int j = start; j &lt;= end; j++) {
		    Rectangle pastRect = rects[j];
		    if (j &gt; start) {
			pastRect.y = rects[j - 1].y + rects[j - 1].height;
		    }
		    pastRect.height += Math.round((float) pastRect.height * factor);
		}
		lastRect.height = max - lastRect.y;
	    }
	}

    }

    /** Tab rects */
    protected Rectangle rects[] = new Rectangle[0];

}

