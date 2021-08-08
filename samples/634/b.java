import java.nio.file.*;
import java.util.Objects;
import sun.security.util.FilePermCompat;

class FilePermission extends Permission implements Serializable {
    /**
     * Checks if the Permission's actions are a proper subset of the
     * this object's actions. Returns the effective mask iff the
     * this FilePermission's path also implies that FilePermission's path.
     *
     * @param that the FilePermission to check against.
     * @return the effective mask
     */
    boolean impliesIgnoreMask(FilePermission that) {
	if (FilePermCompat.nb) {
	    if (this == that) {
		return true;
	    }
	    if (allFiles) {
		return true;
	    }
	    if (this.invalid || that.invalid) {
		return false;
	    }
	    if (that.allFiles) {
		return false;
	    }
	    // Left at least same level of wildness as right
	    if ((this.recursive && that.recursive) != that.recursive
		    || (this.directory && that.directory) != that.directory) {
		return false;
	    }
	    // Same npath is good as long as both or neither are directories
	    if (this.npath.equals(that.npath) && this.directory == that.directory) {
		return true;
	    }
	    int diff = containsPath(this.npath, that.npath);
	    // Right inside left is good if recursive
	    if (diff &gt;= 1 && recursive) {
		return true;
	    }
	    // Right right inside left if it is element in set
	    if (diff == 1 && directory && !that.directory) {
		return true;
	    }

	    // Hack: if a npath2 field exists, apply the same checks
	    // on it as a fallback.
	    if (this.npath2 != null) {
		if (this.npath2.equals(that.npath) && this.directory == that.directory) {
		    return true;
		}
		diff = containsPath(this.npath2, that.npath);
		if (diff &gt;= 1 && recursive) {
		    return true;
		}
		if (diff == 1 && directory && !that.directory) {
		    return true;
		}
	    }

	    return false;
	} else {
	    if (this.directory) {
		if (this.recursive) {
		    // make sure that.path is longer then path so
		    // something like /foo/- does not imply /foo
		    if (that.directory) {
			return (that.cpath.length() &gt;= this.cpath.length()) && that.cpath.startsWith(this.cpath);
		    } else {
			return ((that.cpath.length() &gt; this.cpath.length()) && that.cpath.startsWith(this.cpath));
		    }
		} else {
		    if (that.directory) {
			// if the permission passed in is a directory
			// specification, make sure that a non-recursive
			// permission (i.e., this object) can't imply a recursive
			// permission.
			if (that.recursive)
			    return false;
			else
			    return (this.cpath.equals(that.cpath));
		    } else {
			int last = that.cpath.lastIndexOf(File.separatorChar);
			if (last == -1)
			    return false;
			else {
			    // this.cpath.equals(that.cpath.substring(0, last+1));
			    // Use regionMatches to avoid creating new string
			    return (this.cpath.length() == (last + 1))
				    && this.cpath.regionMatches(0, that.cpath, 0, last + 1);
			}
		    }
		}
	    } else if (that.directory) {
		// if this is NOT recursive/wildcarded,
		// do not let it imply a recursive/wildcarded permission
		return false;
	    } else {
		return (this.cpath.equals(that.cpath));
	    }
	}
    }

    private transient boolean allFiles;
    private transient boolean invalid;
    private transient boolean recursive;
    private transient boolean directory;
    private transient Path npath;
    private transient Path npath2;
    private transient String cpath;
    private static final Path EMPTY_PATH = builtInFS.getPath("");
    private static final Path DOTDOT_PATH = builtInFS.getPath("..");

    /**
     * Returns the depth between an outer path p1 and an inner path p2. -1
     * is returned if
     *
     * - p1 does not contains p2.
     * - this is not decidable. For example, p1="../x", p2="y".
     * - the depth is not decidable. For example, p1="/", p2="x".
     *
     * This method can return 2 if the depth is greater than 2.
     *
     * @param p1 the expected outer path, normalized
     * @param p2 the expected inner path, normalized
     * @return the depth in between
     */
    private static int containsPath(Path p1, Path p2) {

	// Two paths must have the same root. For example,
	// there is no contains relation between any two of
	// "/x", "x", "C:/x", "C:x", and "//host/share/x".
	if (!Objects.equals(p1.getRoot(), p2.getRoot())) {
	    return -1;
	}

	// Empty path (i.e. "." or "") is a strange beast,
	// because its getNameCount()==1 but getName(0) is null.
	// It's better to deal with it separately.
	if (p1.equals(EMPTY_PATH)) {
	    if (p2.equals(EMPTY_PATH)) {
		return 0;
	    } else if (p2.getName(0).equals(DOTDOT_PATH)) {
		// "." contains p2 iff p2 has no "..". Since
		// a normalized path can only have 0 or more
		// ".." at the beginning. We only need to look
		// at the head.
		return -1;
	    } else {
		// and the distance is p2's name count. i.e.
		// 3 between "." and "a/b/c".
		return p2.getNameCount();
	    }
	} else if (p2.equals(EMPTY_PATH)) {
	    int c1 = p1.getNameCount();
	    if (!p1.getName(c1 - 1).equals(DOTDOT_PATH)) {
		// "." is inside p1 iff p1 is 1 or more "..".
		// For the same reason above, we only need to
		// look at the tail.
		return -1;
	    }
	    // and the distance is the count of ".."
	    return c1;
	}

	// Good. No more empty paths.

	// Common heads are removed

	int c1 = p1.getNameCount();
	int c2 = p2.getNameCount();

	int n = Math.min(c1, c2);
	int i = 0;
	while (i &lt; n) {
	    if (!p1.getName(i).equals(p2.getName(i)))
		break;
	    i++;
	}

	// for p1 containing p2, p1 must be 0-or-more "..",
	// and p2 cannot have "..". For the same reason, we only
	// check tail of p1 and head of p2.
	if (i &lt; c1 && !p1.getName(c1 - 1).equals(DOTDOT_PATH)) {
	    return -1;
	}

	if (i &lt; c2 && p2.getName(i).equals(DOTDOT_PATH)) {
	    return -1;
	}

	// and the distance is the name counts added (after removing
	// the common heads).

	// For example: p1 = "../../..", p2 = "../a".
	// After removing the common heads, they become "../.." and "a",
	// and the distance is (3-1)+(2-1) = 3.
	return c1 - i + c2 - i;
    }

}

