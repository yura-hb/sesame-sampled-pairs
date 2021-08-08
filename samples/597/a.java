import java.nio.ByteBuffer;

class AeronNDArraySerde extends BinarySerde {
    /**
     * Get the direct byte buffer from the given direct buffer
     * @param directBuffer
     * @return
     */
    public static ByteBuffer getDirectByteBuffer(DirectBuffer directBuffer) {
	return directBuffer.byteBuffer() == null
		? ByteBuffer.allocateDirect(directBuffer.capacity()).put(directBuffer.byteArray())
		: directBuffer.byteBuffer();
    }

}

