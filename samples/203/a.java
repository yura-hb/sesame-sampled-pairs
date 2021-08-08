import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.factory.Nd4j;
import java.util.ArrayList;
import java.util.List;

class WorkspaceUtils {
    /**
     * Assert that no workspaces are currently open
     *
     * @param msg Message to include in the exception, if required
     */
    public static void assertNoWorkspacesOpen(String msg) throws ND4JWorkspaceException {
	if (Nd4j.getWorkspaceManager().anyWorkspaceActiveForCurrentThread()) {
	    List&lt;MemoryWorkspace&gt; l = Nd4j.getWorkspaceManager().getAllWorkspacesForCurrentThread();
	    List&lt;String&gt; workspaces = new ArrayList&lt;&gt;(l.size());
	    for (MemoryWorkspace ws : l) {
		if (ws.isScopeActive()) {
		    workspaces.add(ws.getId());
		}
	    }
	    throw new ND4JWorkspaceException(msg + " - Open/active workspaces: " + workspaces);
	}
    }

}

