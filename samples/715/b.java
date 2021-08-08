import static sun.nio.fs.WindowsNativeDispatcher.*;
import static sun.nio.fs.WindowsConstants.*;

class WindowsSecurity {
    /**
     * Attempts to enable the given privilege for this method.
     */
    static Privilege enablePrivilege(String priv) {
	final long pLuid;
	try {
	    pLuid = LookupPrivilegeValue(priv);
	} catch (WindowsException x) {
	    // indicates bug in caller
	    throw new AssertionError(x);
	}

	long hToken = 0L;
	boolean impersontating = false;
	boolean elevated = false;
	try {
	    hToken = OpenThreadToken(GetCurrentThread(), TOKEN_ADJUST_PRIVILEGES, false);
	    if (hToken == 0L && processTokenWithDuplicateAccess != 0L) {
		hToken = DuplicateTokenEx(processTokenWithDuplicateAccess,
			(TOKEN_ADJUST_PRIVILEGES | TOKEN_IMPERSONATE));
		SetThreadToken(0L, hToken);
		impersontating = true;
	    }

	    if (hToken != 0L) {
		AdjustTokenPrivileges(hToken, pLuid, SE_PRIVILEGE_ENABLED);
		elevated = true;
	    }
	} catch (WindowsException x) {
	    // nothing to do, privilege not enabled
	}

	final long token = hToken;
	final boolean stopImpersontating = impersontating;
	final boolean needToRevert = elevated;

	return new Privilege() {
	    @Override
	    public void drop() {
		if (token != 0L) {
		    try {
			if (stopImpersontating)
			    SetThreadToken(0L, 0L);
			else if (needToRevert)
			    AdjustTokenPrivileges(token, pLuid, 0);
		    } catch (WindowsException x) {
			// should not happen
			throw new AssertionError(x);
		    } finally {
			CloseHandle(token);
		    }
		}
	    }
	};
    }

    /**
     * Returns the access token for this process with TOKEN_DUPLICATE access
     */
    static final long processTokenWithDuplicateAccess = openProcessToken(TOKEN_DUPLICATE);

}

