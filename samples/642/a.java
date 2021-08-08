import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.memory.abstracts.DummyWorkspace;

abstract class BasicWorkspaceManager implements MemoryWorkspaceManager {
    /**
     * This method temporary opens block out of any workspace scope.
     * &lt;p&gt;
     * PLEASE NOTE: Do not forget to close this block.
     *
     * @return
     */
    @Override
    public MemoryWorkspace scopeOutOfWorkspaces() {
	MemoryWorkspace workspace = Nd4j.getMemoryManager().getCurrentWorkspace();
	if (workspace == null)
	    return new DummyWorkspace();
	else {
	    Nd4j.getMemoryManager().setCurrentWorkspace(null);
	    return workspace.tagOutOfScopeUse();
	}
    }

}

