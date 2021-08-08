import java.awt.dnd.DnDConstants;
import java.util.*;
import sun.awt.dnd.SunDragSourceContextPeer;
import sun.awt.SunToolkit;

class XDragSourceContextPeer extends SunDragSourceContextPeer implements XDragSourceProtocolListener {
    /**
     * The caller must own awtLock.
     */
    public void cleanup(long time) {
	if (dndInProgress) {
	    if (dragProtocol != null) {
		dragProtocol.sendLeaveMessage(time);
	    }

	    if (targetAction != DnDConstants.ACTION_NONE) {
		dragExit(xRoot, yRoot);
	    }

	    dragDropFinished(false, DnDConstants.ACTION_NONE, xRoot, yRoot);
	}

	Iterator&lt;XDragSourceProtocol&gt; dragProtocols = XDragAndDropProtocols.getDragSourceProtocols();
	while (dragProtocols.hasNext()) {
	    XDragSourceProtocol dragProtocol = dragProtocols.next();
	    try {
		dragProtocol.cleanup();
	    } catch (XException xe) {
		// Ignore the exception.
	    }
	}

	dndInProgress = false;
	dragInProgress = false;
	dragRootWindow = 0;
	sourceFormats = null;
	sourceActions = DnDConstants.ACTION_NONE;
	sourceAction = DnDConstants.ACTION_NONE;
	eventState = 0;
	xRoot = 0;
	yRoot = 0;

	cleanupTargetInfo();

	removeDnDGrab(time);
    }

    private boolean dndInProgress = false;
    private XDragSourceProtocol dragProtocol = null;
    private int targetAction = DnDConstants.ACTION_NONE;
    private int xRoot = 0;
    private int yRoot = 0;
    private boolean dragInProgress = false;
    private long dragRootWindow = 0;
    private long[] sourceFormats = null;
    private int sourceActions = DnDConstants.ACTION_NONE;
    private int sourceAction = DnDConstants.ACTION_NONE;
    private int eventState = 0;
    private long targetRootSubwindow = 0;
    private long rootEventMask = 0;
    private static final int ROOT_EVENT_MASK = (int) XConstants.ButtonMotionMask | (int) XConstants.KeyPressMask
	    | (int) XConstants.KeyReleaseMask;

    /**
     * The caller must own awtLock.
     */
    private void cleanupTargetInfo() {
	targetAction = DnDConstants.ACTION_NONE;
	dragProtocol = null;
	targetRootSubwindow = 0;
    }

    private void removeDnDGrab(long time) {
	assert XToolkit.isAWTLockHeldByCurrentThread();

	XlibWrapper.XUngrabPointer(XToolkit.getDisplay(), time);
	XlibWrapper.XUngrabKeyboard(XToolkit.getDisplay(), time);

	/* Restore the root event mask if it was changed. */
	if ((rootEventMask | ROOT_EVENT_MASK) != rootEventMask && dragRootWindow != 0) {

	    XlibWrapper.XSelectInput(XToolkit.getDisplay(), dragRootWindow, rootEventMask);
	}

	rootEventMask = 0;
	dragRootWindow = 0;
    }

}

