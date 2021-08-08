import java.util.ArrayList;
import java.util.Iterator;

class ProgressMonitor {
    /**
     * Return a snapshot of the ProgressSource list
     */
    public ArrayList&lt;ProgressSource&gt; getProgressSources() {
	ArrayList&lt;ProgressSource&gt; snapshot = new ArrayList&lt;&gt;();

	try {
	    synchronized (progressSourceList) {
		for (Iterator&lt;ProgressSource&gt; iter = progressSourceList.iterator(); iter.hasNext();) {
		    ProgressSource pi = iter.next();

		    // Clone ProgressSource and add to snapshot
		    snapshot.add((ProgressSource) pi.clone());
		}
	    }
	} catch (CloneNotSupportedException e) {
	    e.printStackTrace();
	}

	return snapshot;
    }

    private ArrayList&lt;ProgressSource&gt; progressSourceList = new ArrayList&lt;ProgressSource&gt;();

}

