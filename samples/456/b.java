import java.util.Set;

class InstructionHandle {
    /**
     * Denote this handle isn't referenced anymore by t.
     */
    public void removeTargeter(final InstructionTargeter t) {
	if (targeters != null) {
	    targeters.remove(t);
	}
    }

    private Set&lt;InstructionTargeter&gt; targeters;

}

