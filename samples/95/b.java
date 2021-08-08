import com.sun.org.apache.xml.internal.security.transforms.TransformationException;

class TransformXPointer extends TransformSpi {
    /**
     * Method enginePerformTransform
     *
     * @param input
     * @return  {@link XMLSignatureInput} as the result of transformation
     * @throws TransformationException
     */
    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, OutputStream os,
	    Transform transformObject) throws TransformationException {

	Object exArgs[] = { implementedTransformURI };

	throw new TransformationException("signature.Transform.NotYetImplemented", exArgs);
    }

    /** Field implementedTransformURI */
    public static final String implementedTransformURI = Transforms.TRANSFORM_XPOINTER;

}

