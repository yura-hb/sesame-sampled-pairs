import org.nd4j.linalg.primitives.Pair;
import org.nd4j.jita.allocator.impl.AtomicAllocator;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.*;
import org.nd4j.linalg.api.ops.grid.GridPointers;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.nativeblas.LongPointerWrapper;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class CudaGridExecutioner extends CudaExecutioner implements GridExecutioner {
    /**
     * This method adds op into GridOp queue
     *
     * @return
     */
    protected void pushToGrid(OpDescriptor descriptor, boolean flush) {

	// we should just add op to queue here
	//deviceQueues.get().add(descriptor);

	// FIXME: following code should be removed, since it's just executing supers instead of batching

	execCounter.incrementAndGet();

	Op op = descriptor.getOp();
	int[] dimensions = descriptor.getDimensions();

	if (op instanceof TransformOp) {
	    TransformOp t = (TransformOp) op;
	    if (flush)
		flushQueue();

	    //logger.info("Sending TransformOp to CudaExecutioner");
	    super.invoke(t);
	} else if (op instanceof Variance) {
	    Variance acc = (Variance) op;
	    if (flush)
		flushQueue();

	    super.naiveExec(acc, dimensions);
	} else if (op instanceof Accumulation) {
	    Accumulation acc = (Accumulation) op;
	    if (flush)
		flushQueue();

	    //logger.info("Sending AccumulationOp to CudaExecutioner: {}", Arrays.toString(dimensions));
	    super.naiveExec(acc, dimensions);
	} else if (op instanceof ScalarOp) {
	    ScalarOp sc = (ScalarOp) op;
	    if (flush)
		flushQueue();

	    //logger.info("Sending ScalarOp to CudaExecutioner");
	    super.invoke(sc);
	} else if (op instanceof BroadcastOp) {
	    BroadcastOp broadcastOp = (BroadcastOp) op;
	    if (flush)
		flushQueue();

	    //logger.info("Sending BroadcastOp to CudaExecutioner");
	    if (dimensions != null) {
		super.exec(broadcastOp, dimensions);
	    } else {
		super.invoke(broadcastOp);
	    }
	} else if (op instanceof IndexAccumulation) {
	    IndexAccumulation indexAccumulation = (IndexAccumulation) op;
	    if (flush)
		flushQueue();

	    //logger.info("Sending IndexAccumulationOp to CudaExecutioner");
	    super.exec(indexAccumulation, dimensions);
	} else if (op instanceof MetaOp) {
	    //     logger.info("Executing MetaOp");
	    metaCounter.incrementAndGet();
	    exec((MetaOp) op);
	} else if (op instanceof GridOp) {
	    //    logger.info("Executing GridOp");
	    exec((GridOp) op);
	}
    }

    private AtomicLong execCounter = new AtomicLong(0);
    private AtomicLong metaCounter = new AtomicLong(0);
    private ThreadLocal&lt;OpDescriptor&gt; lastOp = new ThreadLocal&lt;&gt;();
    private AtomicBoolean experimental = new AtomicBoolean(false);

    /**
     * This method forces all currently enqueued ops to be executed immediately
     *
     * PLEASE NOTE: This call IS non-blocking
     */
    public void flushQueue() {
	/*
	    Basically we just want to form GridOp and pass it to native executioner
	    But since we don't have GridOp interface yet, we'll send everything to underlying CudaExecutioner.
	 */
	//    logger.info("Non-Blocking flush");
	// TODO: proper implementation for GridOp creation required here
	/*
	Deque&lt;OpDescriptor&gt; currentQueue = deviceQueues.get();
	if (currentQueue == null)
	    return;
	
	OpDescriptor op = currentQueue.pollFirst();
	while (op != null) {
	    pushToGrid(op, false);
	
	    op = currentQueue.pollFirst();
	}
	*/

	// we need to check,
	OpDescriptor op = lastOp.get();
	if (op != null) {
	    if (!experimental.get()) {
		//if (!nativeOps.isExperimentalEnabled()) {
		// it might be only pairwise transform here for now
		//          logger.info("Flushing existing lastOp");
		lastOp.remove();
		dequeueOp(op);
		pushToGrid(op, false);
	    } else {
		throw new UnsupportedOperationException("Experimental flush isn't supported yet");
	    }
	} else {
	    //      logger.info("Queue is empty");

	}
    }

    @Override
    public void exec(MetaOp op) {
	if (extraz.get() == null)
	    extraz.set(new PointerPointer(32));

	prepareGrid(op);

	GridPointers first = op.getGridDescriptor().getGridPointers().get(0);
	GridPointers second = op.getGridDescriptor().getGridPointers().get(1);

	// we need to use it only for first op, since for MetaOps second op shares the same X & Z by definition
	CudaContext context = AtomicAllocator.getInstance().getFlowController().prepareAction(first.getOpZ(),
		first.getOpY());

	//        AtomicAllocator.getInstance().getFlowController().prepareAction(second.getOpX(), second.getOpY(), second.getOpZ());

	//CudaContext context = (CudaContext) AtomicAllocator.getInstance().getDeviceContext().getContext();

	PointerPointer extras = extraz.get().put(null, context.getOldStream());

	double scalarA = 0.0;
	double scalarB = 0.0;

	if (op.getFirstOp() instanceof ScalarOp)
	    scalarA = ((ScalarOp) op.getFirstOp()).scalar().doubleValue();

	if (op.getSecondOp() instanceof ScalarOp)
	    scalarB = ((ScalarOp) op.getSecondOp()).scalar().doubleValue();

	//logger.info("FirstOp: {}, SecondOp: {}", op.getFirstOp().getClass().getSimpleName(), op.getSecondOp().getClass().getSimpleName());

	/*
	    TODO: launch can be either strided, or shapeInfo-based, it doesn't really matters for us.
	    We just need to pass all pointers.
	
	    TODO: obviously, execMetaPredicateElementwiseFloat should be renamed to execMetaPredicateStridedFloat
	 */

	// FIXME: this is bad hack, reconsider this one
	GridPointers yGrid = first;

	if (op.getSecondOp().y() != null) {
	    yGrid = second;
	}

	if (op instanceof PredicateMetaOp || op instanceof InvertedPredicateMetaOp) {
	    if (first.getDtype() == DataBuffer.Type.FLOAT) {
		if (yGrid.getYOrder() == yGrid.getXOrder() && yGrid.getXStride() &gt;= 1 && yGrid.getYStride() &gt;= 1) {
		    nativeOps.execMetaPredicateStridedFloat(extras, first.getType().ordinal(), first.getOpNum(),
			    second.getType().ordinal(), second.getOpNum(), first.getXLength(),
			    (FloatPointer) first.getX(), first.getXStride(), (FloatPointer) yGrid.getY(), // can be null
			    yGrid.getYStride(), // cane be -1
			    (FloatPointer) second.getZ(), second.getZStride(), (FloatPointer) first.getExtraArgs(),
			    (FloatPointer) second.getExtraArgs(), (float) scalarA, (float) scalarB);
		} else {
		    nativeOps.execMetaPredicateShapeFloat(extras, first.getType().ordinal(), first.getOpNum(),
			    second.getType().ordinal(), second.getOpNum(), first.getXLength(),
			    (FloatPointer) first.getX(), (LongPointer) first.getXShapeInfo(),
			    (FloatPointer) yGrid.getY(), // can be null
			    (LongPointer) yGrid.getYShapeInfo(), // cane be -1
			    (FloatPointer) second.getZ(), (LongPointer) second.getZShapeInfo(),
			    (FloatPointer) first.getExtraArgs(), (FloatPointer) second.getExtraArgs(), (float) scalarA,
			    (float) scalarB);
		}
	    } else if (first.getDtype() == DataBuffer.Type.DOUBLE) {
		if (yGrid.getYOrder() == yGrid.getXOrder() && yGrid.getXStride() &gt;= 1 && yGrid.getYStride() &gt;= 1) {
		    nativeOps.execMetaPredicateStridedDouble(extras, first.getType().ordinal(), first.getOpNum(),
			    second.getType().ordinal(), second.getOpNum(), first.getXLength(),
			    (DoublePointer) first.getX(), first.getXStride(), (DoublePointer) yGrid.getY(), // can be null
			    yGrid.getYStride(), // cane be -1
			    (DoublePointer) second.getZ(), second.getZStride(), (DoublePointer) first.getExtraArgs(),
			    (DoublePointer) second.getExtraArgs(), scalarA, scalarB);
		} else {
		    nativeOps.execMetaPredicateShapeDouble(extras, first.getType().ordinal(), first.getOpNum(),
			    second.getType().ordinal(), second.getOpNum(), first.getXLength(),
			    (DoublePointer) first.getX(), (LongPointer) first.getXShapeInfo(),
			    (DoublePointer) yGrid.getY(), // can be null
			    (LongPointer) yGrid.getYShapeInfo(), // cane be -1
			    (DoublePointer) second.getZ(), (LongPointer) second.getZShapeInfo(),
			    (DoublePointer) first.getExtraArgs(), (DoublePointer) second.getExtraArgs(), scalarA,
			    scalarB);
		}
	    } else {
		if (yGrid.getYOrder() == yGrid.getXOrder() && yGrid.getXStride() &gt;= 1 && yGrid.getYStride() &gt;= 1) {
		    nativeOps.execMetaPredicateStridedHalf(extras, first.getType().ordinal(), first.getOpNum(),
			    second.getType().ordinal(), second.getOpNum(), first.getXLength(),
			    (ShortPointer) first.getX(), first.getXStride(), (ShortPointer) yGrid.getY(), // can be null
			    yGrid.getYStride(), // cane be -1
			    (ShortPointer) second.getZ(), second.getZStride(), (ShortPointer) first.getExtraArgs(),
			    (ShortPointer) second.getExtraArgs(), (float) scalarA, (float) scalarB);
		} else {
		    nativeOps.execMetaPredicateShapeHalf(extras, first.getType().ordinal(), first.getOpNum(),
			    second.getType().ordinal(), second.getOpNum(), first.getXLength(),
			    (ShortPointer) first.getX(), (LongPointer) first.getXShapeInfo(),
			    (ShortPointer) yGrid.getY(), // can be null
			    (LongPointer) yGrid.getYShapeInfo(), // cane be -1
			    (ShortPointer) second.getZ(), (LongPointer) second.getZShapeInfo(),
			    (ShortPointer) first.getExtraArgs(), (ShortPointer) second.getExtraArgs(), (float) scalarA,
			    (float) scalarB);
		}
	    }
	} else if (op instanceof ReduceMetaOp) {
	    if (first.getDtype() == DataBuffer.Type.FLOAT) {

		nativeOps.execMetaPredicateReduceFloat(extras, first.getType().ordinal(), first.getOpNum(),
			second.getType().ordinal(), second.getOpNum(), (FloatPointer) first.getX(),
			(LongPointer) first.getXShapeInfo(), (FloatPointer) second.getY(),
			(LongPointer) second.getYShapeInfo(), (FloatPointer) second.getZ(),
			(LongPointer) second.getZShapeInfo(), (IntPointer) second.getDimensions(),
			second.getDimensionsLength(), (LongPointer) second.getTadShape(),
			new LongPointerWrapper(second.getTadOffsets()), (FloatPointer) first.getExtraArgs(),
			(FloatPointer) second.getExtraArgs(), (float) scalarA, 0.0f, false);
	    }
	}

	AtomicAllocator.getInstance().getFlowController().registerAction(context, first.getOpZ(), first.getOpY());
	//        AtomicAllocator.getInstance().getFlowController().registerAction(context, second.getOpX(), second.getOpY(), second.getOpZ());
    }

    @Override
    public void exec(GridOp op) {
	// TODO: to be implemented
    }

    protected void dequeueOp(OpDescriptor descriptor) {

	AtomicAllocator.getInstance().getAllocationPoint(descriptor.getOp().x()).markEnqueued(false);
	AtomicAllocator.getInstance().getAllocationPoint(descriptor.getOp().z()).markEnqueued(false);

	if (descriptor.getOp().y() != null)
	    AtomicAllocator.getInstance().getAllocationPoint(descriptor.getOp().y()).markEnqueued(false);

	//   logger.info("Dequeued op: " + descriptor.getOp().getClass().getSimpleName());
    }

    protected void prepareGrid(MetaOp op) {
	GridPointers ptrA = pointerizeOp(op.getFirstOpDescriptor());
	GridPointers ptrB = pointerizeOp(op.getSecondOpDescriptor());

	op.setFirstPointers(ptrA);
	op.setSecondPointers(ptrB);
    }

    protected GridPointers pointerizeOp(OpDescriptor descriptor) {
	return pointerizeOp(descriptor.getOp(), descriptor.getDimensions());
    }

    /**
     * This method returns Op as set of required pointers for it
     * @param op
     * @param dimensions
     * @return
     */
    protected GridPointers pointerizeOp(Op op, int... dimensions) {
	GridPointers pointers = new GridPointers(op, dimensions);

	AtomicAllocator allocator = AtomicAllocator.getInstance();

	//        CudaContext context = AtomicAllocator.getInstance().getFlowController().prepareAction(op.z(), op.x(), op.y());
	// FIXME: do not leave it as is
	CudaContext context = (CudaContext) allocator.getDeviceContext().getContext();

	pointers.setX(allocator.getPointer(op.x(), context));
	pointers.setXShapeInfo(allocator.getPointer(op.x().shapeInfoDataBuffer(), context));
	pointers.setZ(allocator.getPointer(op.z(), context));
	pointers.setZShapeInfo(allocator.getPointer(op.z().shapeInfoDataBuffer(), context));
	pointers.setZLength(op.z().length());

	if (op.y() != null) {
	    pointers.setY(allocator.getPointer(op.y(), context));
	    pointers.setYShapeInfo(allocator.getPointer(op.y().shapeInfoDataBuffer(), context));
	}

	if (dimensions != null && dimensions.length &gt; 0) {
	    DataBuffer dimensionBuffer = Nd4j.getConstantHandler().getConstantBuffer(dimensions);
	    pointers.setDimensions(allocator.getPointer(dimensionBuffer, context));
	    pointers.setDimensionsLength(dimensions.length);
	}

	// we build TADs
	if (dimensions != null && dimensions.length &gt; 0) {
	    Pair&lt;DataBuffer, DataBuffer&gt; tadBuffers = tadManager.getTADOnlyShapeInfo(op.x(), dimensions);

	    Pointer devTadShapeInfo = AtomicAllocator.getInstance().getPointer(tadBuffers.getFirst(), context);
	    Pointer devTadOffsets = tadBuffers.getSecond() == null ? null
		    : AtomicAllocator.getInstance().getPointer(tadBuffers.getSecond(), context);

	    // we don't really care, if tadOffsets will be nulls
	    pointers.setTadShape(devTadShapeInfo);
	    pointers.setTadOffsets(devTadOffsets);
	}

	return pointers;
    }

}

