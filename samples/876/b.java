import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.util.Vector;

class BasicDirectoryModel extends AbstractListModel&lt;Object&gt; implements PropertyChangeListener {
    /**
     * Returns a list of files.
     *
     * @return a list of files
     */
    public Vector&lt;File&gt; getFiles() {
	synchronized (fileCache) {
	    if (files != null) {
		return files;
	    }
	    files = new Vector&lt;File&gt;();
	    directories = new Vector&lt;File&gt;();
	    directories.addElement(
		    filechooser.getFileSystemView().createFileObject(filechooser.getCurrentDirectory(), ".."));

	    for (int i = 0; i &lt; getSize(); i++) {
		File f = fileCache.get(i);
		if (filechooser.isTraversable(f)) {
		    directories.add(f);
		} else {
		    files.add(f);
		}
	    }
	    return files;
	}
    }

    private Vector&lt;File&gt; fileCache = new Vector&lt;File&gt;(50);
    private Vector&lt;File&gt; files = null;
    private Vector&lt;File&gt; directories = null;
    private JFileChooser filechooser = null;

    public int getSize() {
	return fileCache.size();
    }

}

