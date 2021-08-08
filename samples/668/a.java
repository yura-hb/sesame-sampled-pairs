import org.nd4j.linalg.api.buffer.DataBuffer;

class OnnxGraphMapper extends BaseGraphMapper&lt;GraphProto, NodeProto, AttributeProto, Tensor&gt; {
    /**
     * Convert an onnx type to the proper nd4j type
     * @param dataType the data type to convert
     * @return the nd4j type for the onnx type
     */
    public DataBuffer.Type nd4jTypeFromOnnxType(OnnxProto3.TensorProto.DataType dataType) {
	switch (dataType) {
	case DOUBLE:
	    return DataBuffer.Type.DOUBLE;
	case FLOAT:
	    return DataBuffer.Type.FLOAT;
	case FLOAT16:
	    return DataBuffer.Type.HALF;
	case INT32:
	case INT64:
	    return DataBuffer.Type.INT;
	default:
	    return DataBuffer.Type.UNKNOWN;
	}
    }

}

