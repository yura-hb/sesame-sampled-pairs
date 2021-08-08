import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.memory.abstracts.DummyWorkspace;
import java.util.Set;

class SpTree implements Serializable {
    /**
     * Subdivide the node in to
     * 4 children
     */
    public void subDivide() {
	MemoryWorkspace workspace = workspaceMode == WorkspaceMode.NONE ? new DummyWorkspace()
		: Nd4j.getWorkspaceManager().getWorkspaceForCurrentThread(workspaceConfigurationExternal,
			workspaceExternal);
	try (MemoryWorkspace ws = workspace.notifyScopeEntered()) {

	    INDArray newCorner = Nd4j.create(D);
	    INDArray newWidth = Nd4j.create(D);
	    for (int i = 0; i &lt; numChildren; i++) {
		int div = 1;
		for (int d = 0; d &lt; D; d++) {
		    newWidth.putScalar(d, .5 * boundary.width(d));
		    if ((i / div) % 2 == 1)
			newCorner.putScalar(d, boundary.corner(d) - .5 * boundary.width(d));
		    else
			newCorner.putScalar(d, boundary.corner(d) + .5 * boundary.width(d));
		    div *= 2;
		}

		children[i] = new SpTree(this, data, newCorner, newWidth, indices);

	    }

	    // Move existing points to correct children
	    for (int i = 0; i &lt; size; i++) {
		boolean success = false;
		for (int j = 0; j &lt; this.numChildren; j++)
		    if (!success)
			success = children[j].insert(index[i]);

		index[i] = -1;
	    }

	    // Empty parent node
	    size = 0;
	    isLeaf = false;
	}
    }

    @Getter
    @Setter
    protected WorkspaceMode workspaceMode = WorkspaceMode.NONE;
    protected final static WorkspaceConfiguration workspaceConfigurationExternal = WorkspaceConfiguration.builder()
	    .initialSize(0).overallocationLimit(0.3).policyLearning(LearningPolicy.FIRST_LOOP)
	    .policyReset(ResetPolicy.BLOCK_LEFT).policySpill(SpillPolicy.REALLOCATE)
	    .policyAllocation(AllocationPolicy.OVERALLOCATE).build();
    public final static String workspaceExternal = "SPTREE_LOOP_EXTERNAL";
    private int D;
    private int numChildren = 2;
    private Cell boundary;
    private SpTree[] children;
    private INDArray data;
    private Set&lt;INDArray&gt; indices;
    private int size;
    private int[] index;
    private boolean isLeaf = true;
    private int cumSize;
    private INDArray centerOfMass;
    private int nodeCapacity;
    private SpTree parent;
    private int N;
    private String similarityFunction = "euclidean";
    public final static int NODE_RATIO = 8000;
    private INDArray buf;

    public SpTree(SpTree parent, INDArray data, INDArray corner, INDArray width, Set&lt;INDArray&gt; indices) {
	this(parent, data, corner, width, indices, "euclidean");
    }

    private boolean insert(int index) {
	MemoryWorkspace workspace = workspaceMode == WorkspaceMode.NONE ? new DummyWorkspace()
		: Nd4j.getWorkspaceManager().getWorkspaceForCurrentThread(workspaceConfigurationExternal,
			workspaceExternal);
	try (MemoryWorkspace ws = workspace.notifyScopeEntered()) {

	    INDArray point = data.slice(index);
	    if (!boundary.contains(point))
		return false;

	    cumSize++;
	    double mult1 = (double) (cumSize - 1) / (double) cumSize;
	    double mult2 = 1.0 / (double) cumSize;
	    centerOfMass.muli(mult1);
	    centerOfMass.addi(point.mul(mult2));
	    // If there is space in this quad tree and it is a leaf, add the object here
	    if (isLeaf() && size &lt; nodeCapacity) {
		this.index[size] = index;
		indices.add(point);
		size++;
		return true;
	    }

	    for (int i = 0; i &lt; size; i++) {
		INDArray compPoint = data.slice(this.index[i]);
		if (compPoint.equals(point))
		    return true;
	    }

	    if (isLeaf())
		subDivide();

	    // Find out where the point can be inserted
	    for (int i = 0; i &lt; numChildren; i++) {
		if (children[i].insert(index))
		    return true;
	    }

	    throw new IllegalStateException("Shouldn't reach this state");
	}
    }

    public SpTree(SpTree parent, INDArray data, INDArray corner, INDArray width, Set&lt;INDArray&gt; indices,
	    String similarityFunction) {
	init(parent, data, corner, width, indices, similarityFunction);
    }

    public boolean isLeaf() {
	return isLeaf;
    }

    private void init(SpTree parent, INDArray data, INDArray corner, INDArray width, Set&lt;INDArray&gt; indices,
	    String similarityFunction) {

	this.parent = parent;
	D = data.columns();
	N = data.rows();
	this.similarityFunction = similarityFunction;
	nodeCapacity = N % NODE_RATIO;
	index = new int[nodeCapacity];
	for (int d = 1; d &lt; this.D; d++)
	    numChildren *= 2;
	this.indices = indices;
	isLeaf = true;
	size = 0;
	cumSize = 0;
	children = new SpTree[numChildren];
	this.data = data;
	boundary = new Cell(D);
	boundary.setCorner(corner.dup());
	boundary.setWidth(width.dup());
	centerOfMass = Nd4j.create(D);
	buf = Nd4j.create(D);
    }

}

