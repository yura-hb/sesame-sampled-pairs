import java.io.File;

class Win32ShellFolder2 extends ShellFolder {
    /**
     * @return Whether this shell folder is a link
     */
    public boolean isLink() {
	if (cachedIsLink == null) {
	    cachedIsLink = hasAttribute(ATTRIB_LINK);
	}

	return cachedIsLink;
    }

    private volatile Boolean cachedIsLink;
    public static final int ATTRIB_LINK = 0x00010000;
    FolderDisposer disposer = new FolderDisposer();
    private Boolean isDir = null;
    public static final int ATTRIB_FOLDER = 0x20000000;
    public static final int ATTRIB_BROWSABLE = 0x08000000;

    /**
     * Return whether the given attribute flag is set for this object
     */
    public boolean hasAttribute(final int attribute) {
	Boolean result = invoke(new Callable&lt;Boolean&gt;() {
	    public Boolean call() {
		// Caching at this point doesn't seem to be cost efficient
		return (getAttributes0(getParentIShellFolder(), getRelativePIDL(), attribute) & attribute) != 0;
	    }
	});

	return result != null && result;
    }

    /**
     * Get the parent ShellFolder's IShellFolder interface
     */
    public long getParentIShellFolder() {
	Win32ShellFolder2 parent = (Win32ShellFolder2) getParentFile();
	if (parent == null) {
	    // Parent should only be null if this is the desktop, whose
	    // relativePIDL is relative to its own IShellFolder.
	    return getIShellFolder();
	}
	return parent.getIShellFolder();
    }

    /**
     * Accessor for relative PIDL
     */
    public long getRelativePIDL() {
	if (disposer.relativePIDL == 0) {
	    throw new InternalError("Should always have a relative PIDL");
	}
	return disposer.relativePIDL;
    }

    /**
     * Returns the queried attributes specified in attrsMask.
     *
     * Could plausibly be used for attribute caching but have to be
     * very careful not to touch network drives and file system roots
     * with a full attrsMask
     * NOTE: this method uses COM and must be called on the 'COM thread'. See ComInvoker for the details
     */

    private static native int getAttributes0(long pParentIShellFolder, long pIDL, int attrsMask);

    /**
     * @return The parent shell folder of this shell folder, null if
     * there is no parent
     */
    public File getParentFile() {
	return parent;
    }

    /**
     * Accessor for IShellFolder
     */
    private long getIShellFolder() {
	if (disposer.pIShellFolder == 0) {
	    try {
		disposer.pIShellFolder = invoke(new Callable&lt;Long&gt;() {
		    public Long call() {
			assert (isDirectory());
			assert (parent != null);
			long parentIShellFolder = getParentIShellFolder();
			if (parentIShellFolder == 0) {
			    throw new InternalError("Parent IShellFolder was null for " + getAbsolutePath());
			}
			// We are a directory with a parent and a relative PIDL.
			// We want to bind to the parent so we get an
			// IShellFolder instance associated with us.
			long pIShellFolder = bindToObject(parentIShellFolder, disposer.relativePIDL);
			if (pIShellFolder == 0) {
			    throw new InternalError("Unable to bind " + getAbsolutePath() + " to parent");
			}
			return pIShellFolder;
		    }
		}, RuntimeException.class);
	    } catch (InterruptedException e) {
		// Ignore error
	    }
	}
	return disposer.pIShellFolder;
    }

    public boolean isDirectory() {
	if (isDir == null) {
	    // Folders with SFGAO_BROWSABLE have "shell extension" handlers and are
	    // not traversable in JFileChooser.
	    if (hasAttribute(ATTRIB_FOLDER) && !hasAttribute(ATTRIB_BROWSABLE)) {
		isDir = Boolean.TRUE;
	    } else if (isLink()) {
		ShellFolder linkLocation = getLinkLocation(false);
		isDir = Boolean.valueOf(linkLocation != null && linkLocation.isDirectory());
	    } else {
		isDir = Boolean.FALSE;
	    }
	}
	return isDir.booleanValue();
    }

    private static native long bindToObject(long parentIShellFolder, long pIDL);

    private Win32ShellFolder2 getLinkLocation(final boolean resolve) {
	return invoke(new Callable&lt;Win32ShellFolder2&gt;() {
	    public Win32ShellFolder2 call() {
		if (!isLink()) {
		    return null;
		}

		Win32ShellFolder2 location = null;
		long linkLocationPIDL = getLinkLocation(getParentIShellFolder(), getRelativePIDL(), resolve);
		if (linkLocationPIDL != 0) {
		    try {
			location = Win32ShellFolderManager2.createShellFolderFromRelativePIDL(getDesktop(),
				linkLocationPIDL);
		    } catch (InterruptedException e) {
			// Return null
		    } catch (InternalError e) {
			// Could be a link to a non-bindable object, such as a network connection
			// TODO: getIShellFolder() should throw FileNotFoundException instead
		    }
		}
		return location;
	    }
	});
    }

    private static native long getLinkLocation(long parentIShellFolder, long relativePIDL, boolean resolve);

    /**
     * Helper function to return the desktop
     */
    public Win32ShellFolder2 getDesktop() {
	return Win32ShellFolderManager2.getDesktop();
    }

}

