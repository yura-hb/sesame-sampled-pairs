import nsk.share.*;

class EventPacket extends CommandPacket {
    /**
     * Return suspend policy of the events in the packet.
     *
     * throws BoundException if event packet structure is not valid
     */
    public byte getSuspendPolicy() {
	try {
	    return getByte(SuspendPolicyOffset);
	} catch (BoundException e) {
	    throw new Failure("Caught unexpected exception while getting event kind from header:\n\t" + e);
	}
    }

    /** Offset of the "suspendPolicy" field in a JDWP event packet. */
    public final static int SuspendPolicyOffset = DataOffset;

}

