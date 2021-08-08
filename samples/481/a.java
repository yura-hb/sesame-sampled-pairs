import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ModificationLog {
    /**
     * Returns information about the last write to the given address range
     */
    public MemoryAccessLog getReportFor(long address, int size) {
	List&lt;Tag&gt; tags = new ArrayList&lt;&gt;();
	tags.addAll(this.operationStack);

	List&lt;MemoryOperation&gt; operations = new ArrayList&lt;&gt;();
	if (this.buffer0 != null) {
	    int pointerToStart = (this.insertionPosition + this.buffer0.length - this.currentEntries)
		    % this.buffer0.length;
	    int currentPosition = (this.insertionPosition + this.buffer0.length - 1) % this.buffer0.length;
	    long currentWrite = this.timer;
	    do {
		long nextAddress = this.buffer0[currentPosition];
		int nextArgument = this.buffer1[currentPosition];
		byte nextOp = this.operation[currentPosition];

		switch (nextOp) {
		case POP_OPERATION: {
		    tags.add(getTagForId(nextArgument));
		    break;
		}
		case PUSH_OPERATION: {
		    tags.remove(tags.size() - 1);
		    break;
		}
		default: {
		    boolean isMatch = false;
		    if (address &lt; nextAddress) {
			long diff = nextAddress - address;
			if (diff &lt; size) {
			    isMatch = true;
			}
		    } else {
			long diff = address - nextAddress;
			if (diff &lt; nextArgument) {
			    isMatch = true;
			}
		    }

		    if (isMatch) {
			List&lt;Tag&gt; stack = new ArrayList&lt;&gt;();
			stack.addAll(tags);
			MemoryOperation nextOperation = new MemoryOperation(nextOp, currentWrite, nextAddress,
				nextArgument, stack);
			operations.add(nextOperation);
		    }

		    currentWrite--;
		}
		}
		currentPosition = (currentPosition + this.buffer0.length - 1) % this.buffer0.length;
	    } while (currentPosition != pointerToStart);
	}
	return new MemoryAccessLog(operations);
    }

    private final ArrayDeque&lt;Tag&gt; operationStack = new ArrayDeque&lt;&gt;();
    private long[] buffer0;
    private int insertionPosition;
    private int currentEntries;
    private long timer;
    private int[] buffer1;
    private byte[] operation;
    public static final byte POP_OPERATION = 1;
    public static final byte PUSH_OPERATION = 0;
    private static Map&lt;Integer, Tag&gt; activeTags = new HashMap&lt;&gt;();

    private Tag getTagForId(int nextArgument) {
	return activeTags.get(nextArgument);
    }

    class MemoryOperation {
	private final ArrayDeque&lt;Tag&gt; operationStack = new ArrayDeque&lt;&gt;();
	private long[] buffer0;
	private int insertionPosition;
	private int currentEntries;
	private long timer;
	private int[] buffer1;
	private byte[] operation;
	public static final byte POP_OPERATION = 1;
	public static final byte PUSH_OPERATION = 0;
	private static Map&lt;Integer, Tag&gt; activeTags = new HashMap&lt;&gt;();

	public MemoryOperation(byte operationType, long time, long startAddress, int size, List&lt;Tag&gt; stack) {
	    super();
	    this.operationType = operationType;
	    this.time = time;
	    this.startAddress = startAddress;
	    this.addressSize = size;
	    this.stack = stack;
	}

    }

    class MemoryAccessLog {
	private final ArrayDeque&lt;Tag&gt; operationStack = new ArrayDeque&lt;&gt;();
	private long[] buffer0;
	private int insertionPosition;
	private int currentEntries;
	private long timer;
	private int[] buffer1;
	private byte[] operation;
	public static final byte POP_OPERATION = 1;
	public static final byte PUSH_OPERATION = 0;
	private static Map&lt;Integer, Tag&gt; activeTags = new HashMap&lt;&gt;();

	public MemoryAccessLog(List&lt;MemoryOperation&gt; operations) {
	    super();
	    this.operations = operations;
	}

    }

}

