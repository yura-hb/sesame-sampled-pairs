import java.nio.file.*;
import java.io.IOException;
import static sun.nio.fs.WindowsNativeDispatcher.*;
import static sun.nio.fs.WindowsConstants.*;

class WindowsFileCopy {
    /**
     * Copy file from source to target
     */
    static void copy(final WindowsPath source, final WindowsPath target, CopyOption... options) throws IOException {
	// map options
	boolean replaceExisting = false;
	boolean copyAttributes = false;
	boolean followLinks = true;
	boolean interruptible = false;
	for (CopyOption option : options) {
	    if (option == StandardCopyOption.REPLACE_EXISTING) {
		replaceExisting = true;
		continue;
	    }
	    if (option == LinkOption.NOFOLLOW_LINKS) {
		followLinks = false;
		continue;
	    }
	    if (option == StandardCopyOption.COPY_ATTRIBUTES) {
		copyAttributes = true;
		continue;
	    }
	    if (ExtendedOptions.INTERRUPTIBLE.matches(option)) {
		interruptible = true;
		continue;
	    }
	    if (option == null)
		throw new NullPointerException();
	    throw new UnsupportedOperationException("Unsupported copy option");
	}

	// check permissions. If the source file is a symbolic link then
	// later we must also check LinkPermission
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    source.checkRead();
	    target.checkWrite();
	}

	// get attributes of source file
	// attempt to get attributes of target file
	// if both files are the same there is nothing to do
	// if target exists and !replace then throw exception

	WindowsFileAttributes sourceAttrs = null;
	WindowsFileAttributes targetAttrs = null;

	long sourceHandle = 0L;
	try {
	    sourceHandle = source.openForReadAttributeAccess(followLinks);
	} catch (WindowsException x) {
	    x.rethrowAsIOException(source);
	}
	try {
	    // source attributes
	    try {
		sourceAttrs = WindowsFileAttributes.readAttributes(sourceHandle);
	    } catch (WindowsException x) {
		x.rethrowAsIOException(source);
	    }

	    // open target (don't follow links)
	    long targetHandle = 0L;
	    try {
		targetHandle = target.openForReadAttributeAccess(false);
		try {
		    targetAttrs = WindowsFileAttributes.readAttributes(targetHandle);

		    // if both files are the same then nothing to do
		    if (WindowsFileAttributes.isSameFile(sourceAttrs, targetAttrs)) {
			return;
		    }

		    // can't replace file
		    if (!replaceExisting) {
			throw new FileAlreadyExistsException(target.getPathForExceptionMessage());
		    }

		} finally {
		    CloseHandle(targetHandle);
		}
	    } catch (WindowsException x) {
		// ignore
	    }

	} finally {
	    CloseHandle(sourceHandle);
	}

	// if source file is a symbolic link then we must check for LinkPermission
	if (sm != null && sourceAttrs.isSymbolicLink()) {
	    sm.checkPermission(new LinkPermission("symbolic"));
	}

	final String sourcePath = asWin32Path(source);
	final String targetPath = asWin32Path(target);

	// if target exists then delete it.
	if (targetAttrs != null) {
	    try {
		if (targetAttrs.isDirectory() || targetAttrs.isDirectoryLink()) {
		    RemoveDirectory(targetPath);
		} else {
		    DeleteFile(targetPath);
		}
	    } catch (WindowsException x) {
		if (targetAttrs.isDirectory()) {
		    // ERROR_ALREADY_EXISTS is returned when attempting to delete
		    // non-empty directory on SAMBA servers.
		    if (x.lastError() == ERROR_DIR_NOT_EMPTY || x.lastError() == ERROR_ALREADY_EXISTS) {
			throw new DirectoryNotEmptyException(target.getPathForExceptionMessage());
		    }
		}
		x.rethrowAsIOException(target);
	    }
	}

	// Use CopyFileEx if the file is not a directory or junction
	if (!sourceAttrs.isDirectory() && !sourceAttrs.isDirectoryLink()) {
	    final int flags = (!followLinks) ? COPY_FILE_COPY_SYMLINK : 0;

	    if (interruptible) {
		// interruptible copy
		Cancellable copyTask = new Cancellable() {
		    @Override
		    public int cancelValue() {
			return 1; // TRUE
		    }

		    @Override
		    public void implRun() throws IOException {
			try {
			    CopyFileEx(sourcePath, targetPath, flags, addressToPollForCancel());
			} catch (WindowsException x) {
			    x.rethrowAsIOException(source, target);
			}
		    }
		};
		try {
		    Cancellable.runInterruptibly(copyTask);
		} catch (ExecutionException e) {
		    Throwable t = e.getCause();
		    if (t instanceof IOException)
			throw (IOException) t;
		    throw new IOException(t);
		}
	    } else {
		// non-interruptible copy
		try {
		    CopyFileEx(sourcePath, targetPath, flags, 0L);
		} catch (WindowsException x) {
		    x.rethrowAsIOException(source, target);
		}
	    }
	    if (copyAttributes) {
		// CopyFileEx does not copy security attributes
		try {
		    copySecurityAttributes(source, target, followLinks);
		} catch (IOException x) {
		    // ignore
		}
	    }
	    return;
	}

	// copy directory or directory junction
	try {
	    if (sourceAttrs.isDirectory()) {
		CreateDirectory(targetPath, 0L);
	    } else {
		String linkTarget = WindowsLinkSupport.readLink(source);
		int flags = SYMBOLIC_LINK_FLAG_DIRECTORY;
		CreateSymbolicLink(targetPath, WindowsPath.addPrefixIfNeeded(linkTarget), flags);
	    }
	} catch (WindowsException x) {
	    x.rethrowAsIOException(target);
	}
	if (copyAttributes) {
	    // copy DOS/timestamps attributes
	    WindowsFileAttributeViews.Dos view = WindowsFileAttributeViews.createDosView(target, false);
	    try {
		view.setAttributes(sourceAttrs);
	    } catch (IOException x) {
		if (sourceAttrs.isDirectory()) {
		    try {
			RemoveDirectory(targetPath);
		    } catch (WindowsException ignore) {
		    }
		}
	    }

	    // copy security attributes. If this fail it doesn't cause the move
	    // to fail.
	    try {
		copySecurityAttributes(source, target, followLinks);
	    } catch (IOException ignore) {
	    }
	}
    }

    private static String asWin32Path(WindowsPath path) throws IOException {
	try {
	    return path.getPathForWin32Calls();
	} catch (WindowsException x) {
	    x.rethrowAsIOException(path);
	    return null;
	}
    }

    /**
     * Copy DACL/owner/group from source to target
     */
    private static void copySecurityAttributes(WindowsPath source, WindowsPath target, boolean followLinks)
	    throws IOException {
	String path = WindowsLinkSupport.getFinalPath(source, followLinks);

	// may need SeRestorePrivilege to set file owner
	WindowsSecurity.Privilege priv = WindowsSecurity.enablePrivilege("SeRestorePrivilege");
	try {
	    int request = (DACL_SECURITY_INFORMATION | OWNER_SECURITY_INFORMATION | GROUP_SECURITY_INFORMATION);
	    NativeBuffer buffer = WindowsAclFileAttributeView.getFileSecurity(path, request);
	    try {
		try {
		    SetFileSecurity(target.getPathForWin32Calls(), request, buffer.address());
		} catch (WindowsException x) {
		    x.rethrowAsIOException(target);
		}
	    } finally {
		buffer.release();
	    }
	} finally {
	    priv.drop();
	}
    }

}

