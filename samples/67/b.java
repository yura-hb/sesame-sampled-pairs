import sun.jvmstat.perfdata.monitor.*;
import java.nio.*;

class PerfDataBufferPrologue extends AbstractPerfDataBufferPrologue {
    /**
     * Get the offset of the first PerfDataEntry.
     */
    public int getEntryOffset() {
	byteBuffer.position(PERFDATA_PROLOG_ENTRYOFFSET_OFFSET);
	return byteBuffer.getInt();
    }

    final static int PERFDATA_PROLOG_ENTRYOFFSET_OFFSET = 24;

}

