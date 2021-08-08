import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.lir.alloc.lsra.Interval.SpillState;

class LinearScanLifetimeAnalysisPhase extends LinearScanAllocationPhase {
    /**
     * Eliminates moves from register to stack if the stack slot is known to be correct.
     *
     * @param op
     * @param operand
     */
    protected void changeSpillDefinitionPos(LIRInstruction op, AllocatableValue operand, Interval interval,
	    int defPos) {
	assert interval.isSplitParent() : "can only be called for split parents";

	switch (interval.spillState()) {
	case NoDefinitionFound:
	    assert interval.spillDefinitionPos() == -1 : "must no be set before";
	    interval.setSpillDefinitionPos(defPos);
	    interval.setSpillState(SpillState.NoSpillStore);
	    break;

	case NoSpillStore:
	    assert defPos &lt;= interval
		    .spillDefinitionPos() : "positions are processed in reverse order when intervals are created";
	    if (defPos &lt; interval.spillDefinitionPos() - 2) {
		// second definition found, so no spill optimization possible for this interval
		interval.setSpillState(SpillState.NoOptimization);
	    } else {
		// two consecutive definitions (because of two-operand LIR form)
		assert allocator.blockForId(defPos) == allocator
			.blockForId(interval.spillDefinitionPos()) : "block must be equal";
	    }
	    break;

	case NoOptimization:
	    // nothing to do
	    break;

	default:
	    throw GraalError.shouldNotReachHere("other states not allowed at this time");
	}
    }

    protected final LinearScan allocator;

}

