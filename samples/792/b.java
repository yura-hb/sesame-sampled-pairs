import nsk.share.*;

class Packet extends ByteBuffer {
    /**
     * Clear buffer of the packet.
     */
    public void resetBuffer() {
	super.resetBuffer();
	while (length() &lt; PacketHeaderSize)
	    addByte((byte) 0);
	setLength();
	resetPosition();
    }

    /** Size of JDWP packet header. */
    public final static int PacketHeaderSize = DataOffset;
    /** Offset of "length" field of JDWP packet. */
    public final static int LengthOffset = 0;

    /**
     * Assign packet length value to the "length" field of JDWP packet.
     */
    public void setLength() {
	setLength(length());
    }

    /**
     * Sets the current parser position to "data" field of JDWP packet.
     */
    public void resetPosition() {
	resetPosition(PacketHeaderSize);
    }

    /**
     * Assign specified value to the "length" field of JDWP packet.
     */
    public void setLength(int length) {
	try {
	    putInt(LengthOffset, length);
	} catch (BoundException e) {
	    throw new Failure("Caught unexpected exception while setting packet length value into header:\n\t" + e);
	}
    }

}

