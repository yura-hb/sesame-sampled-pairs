import sun.util.logging.PlatformLogger;
import sun.awt.*;
import sun.java2d.pipe.Region;

class XComponentPeer extends XWindow implements ComponentPeer, DropTargetPeer, BackBufferCapsProvider {
    /**
     * Applies the shape to the X-window.
     * @since 1.7
     */
    public void applyShape(Region shape) {
	if (XlibUtil.isShapingSupported()) {
	    if (shapeLog.isLoggable(PlatformLogger.Level.FINER)) {
		shapeLog.finer("*** INFO: Setting shape: PEER: " + this + "; WINDOW: " + getWindow() + "; TARGET: "
			+ target + "; SHAPE: " + shape);
	    }
	    XToolkit.awtLock();
	    try {
		if (shape != null) {

		    int scale = getScale();
		    if (scale != 1) {
			shape = shape.getScaledRegion(scale, scale);
		    }

		    XlibWrapper.SetRectangularShape(XToolkit.getDisplay(), getWindow(), shape.getLoX(), shape.getLoY(),
			    shape.getHiX(), shape.getHiY(), (shape.isRectangular() ? null : shape));
		} else {
		    XlibWrapper.SetRectangularShape(XToolkit.getDisplay(), getWindow(), 0, 0, 0, 0, null);
		}
	    } finally {
		XToolkit.awtUnlock();
	    }
	} else {
	    if (shapeLog.isLoggable(PlatformLogger.Level.FINER)) {
		shapeLog.finer("*** WARNING: Shaping is NOT supported!");
	    }
	}
    }

    private static final PlatformLogger shapeLog = PlatformLogger.getLogger("sun.awt.X11.shape.XComponentPeer");

}

