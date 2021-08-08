import org.nd4j.linalg.api.memory.MemoryWorkspace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class BasicWorkspaceManager implements MemoryWorkspaceManager {
    /**
     * This method destroys all workspaces allocated in current thread
     */
    @Override
    public void destroyAllWorkspacesForCurrentThread() {
	ensureThreadExistense();

	List&lt;MemoryWorkspace&gt; workspaces = new ArrayList&lt;&gt;();
	workspaces.addAll(backingMap.get().values());

	for (MemoryWorkspace workspace : workspaces) {
	    destroyWorkspace(workspace);
	}

	System.gc();
    }

    protected ThreadLocal&lt;Map&lt;String, MemoryWorkspace&gt;&gt; backingMap = new ThreadLocal&lt;&gt;();

    protected void ensureThreadExistense() {
	if (backingMap.get() == null)
	    backingMap.set(new HashMap&lt;String, MemoryWorkspace&gt;());
    }

    /**
     * This method destroys given workspace
     *
     * @param workspace
     */
    @Override
    public void destroyWorkspace(MemoryWorkspace workspace) {
	if (workspace == null || workspace instanceof DummyWorkspace)
	    return;

	//workspace.destroyWorkspace();
	backingMap.get().remove(workspace.getId());
    }

}

