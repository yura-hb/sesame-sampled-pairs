import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.*;
import org.nd4j.linalg.profiler.data.StackAggregator;
import org.nd4j.linalg.profiler.data.StringCounter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

class OpProfiler {
    /**
     *
     * @param op
     * @param tadBuffers
     */
    public void processOpCall(Op op, DataBuffer... tadBuffers) {
	processOpCall(op);

	PenaltyCause[] causes = processTADOperands(tadBuffers);
	for (PenaltyCause cause : causes) {
	    switch (cause) {
	    case TAD_NON_EWS_ACCESS:
		tadNonEwsAggregator.incrementCount();
		break;
	    case TAD_STRIDED_ACCESS:
		tadStridedAggregator.incrementCount();
		break;
	    case NONE:
	    default:
		break;
	    }
	}
    }

    @Getter
    private StackAggregator tadNonEwsAggregator = new StackAggregator();
    @Getter
    private StackAggregator tadStridedAggregator = new StackAggregator();
    private AtomicLong invocationsCount = new AtomicLong(0);
    @Getter
    private StringCounter opCounter = new StringCounter();
    @Getter
    private StringCounter classCounter = new StringCounter();
    private long lastZ = 0;
    @Getter
    private StringCounter matchingCounter = new StringCounter();
    private String prevOpMatching = "";
    @Getter
    private StringCounter matchingCounterDetailed = new StringCounter();
    private String prevOpMatchingDetailed = "";
    @Getter
    private StringCounter matchingCounterInverted = new StringCounter();
    private String prevOpMatchingInverted = "";
    @Getter
    private StackAggregator nonEwsAggregator = new StackAggregator();
    @Getter
    private StackAggregator stridedAggregator = new StackAggregator();
    @Getter
    private StackAggregator mixedOrderAggregator = new StackAggregator();
    private List&lt;OpProfilerListener&gt; listeners = new ArrayList&lt;&gt;();
    private String prevOpName = "";
    private String prevOpClass = "";
    @Getter
    private StringCounter classPairsCounter = new StringCounter();
    @Getter
    private StringCounter opPairsCounter = new StringCounter();

    /**
     * This method tracks op calls
     *
     * @param op
     */
    public void processOpCall(Op op) {
	// total number of invocations
	invocationsCount.incrementAndGet();

	// number of invocations for this specific op
	opCounter.incrementCount(op.opName());

	// number of invocations for specific class
	String opClass = getOpClass(op);
	classCounter.incrementCount(opClass);

	if (op.x().data().address() == lastZ && op.z() == op.x() && op.y() == null) {
	    // we have possible shift here
	    matchingCounter.incrementCount(prevOpMatching + " -&gt; " + opClass);
	    matchingCounterDetailed.incrementCount(prevOpMatchingDetailed + " -&gt; " + opClass + " " + op.opName());
	} else {
	    matchingCounter.totalsIncrement();
	    matchingCounterDetailed.totalsIncrement();
	    if (op.y() != null && op.y().data().address() == lastZ) {
		matchingCounterInverted.incrementCount(prevOpMatchingInverted + " -&gt; " + opClass + " " + op.opName());
	    } else {
		matchingCounterInverted.totalsIncrement();
	    }

	}
	lastZ = op.z().data().address();
	prevOpMatching = opClass;
	prevOpMatchingDetailed = opClass + " " + op.opName();
	prevOpMatchingInverted = opClass + " " + op.opName();

	updatePairs(op.opName(), opClass);

	PenaltyCause[] causes = processOperands(op.x(), op.y(), op.z());
	for (PenaltyCause cause : causes) {
	    switch (cause) {
	    case NON_EWS_ACCESS:
		nonEwsAggregator.incrementCount();
		break;
	    case STRIDED_ACCESS:
		stridedAggregator.incrementCount();
		break;
	    case MIXED_ORDER:
		mixedOrderAggregator.incrementCount();
		break;
	    case NONE:
	    default:
		break;
	    }
	}

	for (OpProfilerListener listener : listeners) {
	    listener.invoke(op);
	}
    }

    public PenaltyCause[] processTADOperands(DataBuffer... tadBuffers) {

	List&lt;PenaltyCause&gt; causes = new ArrayList&lt;&gt;();
	for (DataBuffer tadBuffer : tadBuffers) {
	    if (tadBuffer == null)
		continue;

	    int rank = tadBuffer.getInt(0);
	    int length = rank * 2 + 4;
	    int ews = tadBuffer.getInt(length - 2);

	    if ((ews &lt; 1 || rank &gt; 2 || (rank == 2 && tadBuffer.getInt(1) &gt; 1 && tadBuffer.getInt(2) &gt; 1))
		    && !causes.contains(PenaltyCause.TAD_NON_EWS_ACCESS))
		causes.add(PenaltyCause.TAD_NON_EWS_ACCESS);
	    else if (ews &gt; 1 && !causes.contains(PenaltyCause.TAD_STRIDED_ACCESS))
		causes.add(PenaltyCause.TAD_STRIDED_ACCESS);
	}

	if (causes.isEmpty())
	    causes.add(NONE);

	return causes.toArray(new PenaltyCause[0]);
    }

    /**
     * This method returns op class opName
     *
     * @param op
     * @return
     */
    protected String getOpClass(Op op) {
	if (op instanceof ScalarOp) {
	    return "ScalarOp";
	} else if (op instanceof MetaOp) {
	    return "MetaOp";
	} else if (op instanceof GridOp) {
	    return "GridOp";
	} else if (op instanceof BroadcastOp) {
	    return "BroadcastOp";
	} else if (op instanceof RandomOp) {
	    return "RandomOp";
	} else if (op instanceof Accumulation) {
	    return "AccumulationOp";
	} else if (op instanceof TransformOp) {
	    if (op.y() == null) {
		return "TransformOp";
	    } else
		return "PairWiseTransformOp";
	} else if (op instanceof IndexAccumulation) {
	    return "IndexAccumulationOp";
	} else if (op instanceof CustomOp) {
	    return "CustomOp";
	} else
	    return "Unknown Op calls";
    }

    protected void updatePairs(String opName, String opClass) {
	// now we save pairs of ops/classes
	String cOpNameKey = prevOpName + " -&gt; " + opName;
	String cOpClassKey = prevOpClass + " -&gt; " + opClass;

	classPairsCounter.incrementCount(cOpClassKey);
	opPairsCounter.incrementCount(cOpNameKey);

	prevOpName = opName;
	prevOpClass = opClass;
    }

    public PenaltyCause[] processOperands(INDArray x, INDArray y, INDArray z) {
	if (y == null)
	    return processOperands(x, z);

	if (x == z || y == z) {
	    return processOperands(x, y);
	} else {
	    PenaltyCause causeXY[] = processOperands(x, y);
	    PenaltyCause causeXZ[] = processOperands(x, z);

	    if ((causeXY.length == 1 && causeXY[0] == NONE) && (causeXZ.length == 1 && causeXZ[0] == NONE)) {
		return causeXY;
	    } else if (causeXY.length == 1 && causeXY[0] == NONE) {
		return causeXZ;
	    } else if (causeXZ.length == 1 && causeXZ[0] == NONE) {
		return causeXY;
	    } else
		return joinDistinct(causeXY, causeXZ);
	}
    }

    public PenaltyCause[] processOperands(INDArray x, INDArray y) {
	List&lt;PenaltyCause&gt; penalties = new ArrayList&lt;&gt;();

	if (x.ordering() != y.ordering()) {
	    penalties.add(PenaltyCause.MIXED_ORDER);
	}

	if (x.elementWiseStride() &lt; 1) {
	    penalties.add(PenaltyCause.NON_EWS_ACCESS);
	} else if (y.elementWiseStride() &lt; 1) {
	    penalties.add(PenaltyCause.NON_EWS_ACCESS);
	}

	if (x.elementWiseStride() &gt; 1) {
	    penalties.add(PenaltyCause.STRIDED_ACCESS);
	} else if (y.elementWiseStride() &gt; 1) {
	    penalties.add(PenaltyCause.STRIDED_ACCESS);
	}

	if (penalties.isEmpty())
	    penalties.add(NONE);

	return penalties.toArray(new PenaltyCause[0]);
    }

    protected PenaltyCause[] joinDistinct(PenaltyCause[] a, PenaltyCause[] b) {
	List&lt;PenaltyCause&gt; causes = new ArrayList&lt;&gt;();

	for (PenaltyCause cause : a) {
	    if (cause != null && !causes.contains(cause))
		causes.add(cause);
	}

	for (PenaltyCause cause : b) {
	    if (cause != null && !causes.contains(cause))
		causes.add(cause);
	}

	return causes.toArray(new PenaltyCause[0]);
    }

    interface OpProfilerListener {
	@Getter
	private StackAggregator tadNonEwsAggregator = new StackAggregator();
	@Getter
	private StackAggregator tadStridedAggregator = new StackAggregator();
	private AtomicLong invocationsCount = new AtomicLong(0);
	@Getter
	private StringCounter opCounter = new StringCounter();
	@Getter
	private StringCounter classCounter = new StringCounter();
	private long lastZ = 0;
	@Getter
	private StringCounter matchingCounter = new StringCounter();
	private String prevOpMatching = "";
	@Getter
	private StringCounter matchingCounterDetailed = new StringCounter();
	private String prevOpMatchingDetailed = "";
	@Getter
	private StringCounter matchingCounterInverted = new StringCounter();
	private String prevOpMatchingInverted = "";
	@Getter
	private StackAggregator nonEwsAggregator = new StackAggregator();
	@Getter
	private StackAggregator stridedAggregator = new StackAggregator();
	@Getter
	private StackAggregator mixedOrderAggregator = new StackAggregator();
	private List&lt;OpProfilerListener&gt; listeners = new ArrayList&lt;&gt;();
	private String prevOpName = "";
	private String prevOpClass = "";
	@Getter
	private StringCounter classPairsCounter = new StringCounter();
	@Getter
	private StringCounter opPairsCounter = new StringCounter();

	void invoke(Op op);

    }

}

