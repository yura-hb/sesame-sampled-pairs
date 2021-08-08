import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.InvalidDnDOperationException;
import java.util.Map;
import java.io.IOException;
import sun.awt.AWTPermissions;
import sun.awt.datatransfer.DataTransferer;

abstract class SunDropTargetContextPeer implements DropTargetContextPeer, Transferable {
    /**
     * @return the data
     */

    public Object getTransferData(DataFlavor df)
	    throws UnsupportedFlavorException, IOException, InvalidDnDOperationException {

	SecurityManager sm = System.getSecurityManager();
	try {
	    if (!dropInProcess && sm != null) {
		sm.checkPermission(AWTPermissions.ACCESS_CLIPBOARD_PERMISSION);
	    }
	} catch (Exception e) {
	    Thread currentThread = Thread.currentThread();
	    currentThread.getUncaughtExceptionHandler().uncaughtException(currentThread, e);
	    return null;
	}

	Long lFormat = null;
	Transferable localTransferable = local;

	if (localTransferable != null) {
	    return localTransferable.getTransferData(df);
	}

	if (dropStatus != STATUS_ACCEPT || dropComplete) {
	    throw new InvalidDnDOperationException("No drop current");
	}

	Map&lt;DataFlavor, Long&gt; flavorMap = DataTransferer.getInstance().getFlavorsForFormats(currentT,
		DataTransferer.adaptFlavorMap(currentDT.getFlavorMap()));

	lFormat = flavorMap.get(df);
	if (lFormat == null) {
	    throw new UnsupportedFlavorException(df);
	}

	if (df.isRepresentationClassRemote() && currentDA != DnDConstants.ACTION_LINK) {
	    throw new InvalidDnDOperationException(
		    "only ACTION_LINK is permissable for transfer of java.rmi.Remote objects");
	}

	final long format = lFormat.longValue();

	Object ret = getNativeData(format);

	if (ret instanceof byte[]) {
	    try {
		return DataTransferer.getInstance().translateBytes((byte[]) ret, df, format, this);
	    } catch (IOException e) {
		throw new InvalidDnDOperationException(e.getMessage());
	    }
	} else if (ret instanceof InputStream) {
	    try {
		return DataTransferer.getInstance().translateStream((InputStream) ret, df, format, this);
	    } catch (IOException e) {
		throw new InvalidDnDOperationException(e.getMessage());
	    }
	} else {
	    throw new IOException("no native data was transfered");
	}
    }

    boolean dropInProcess = false;
    private Transferable local;
    protected int dropStatus = STATUS_NONE;
    protected static final int STATUS_ACCEPT = 2;
    protected boolean dropComplete = false;
    private long[] currentT;
    private DropTarget currentDT;
    private int currentDA;

    protected abstract Object getNativeData(long format) throws IOException;

}

