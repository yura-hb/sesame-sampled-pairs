abstract class Certificate implements Serializable {
    class CertificateRep implements Serializable {
	/**
	 * Resolve the Certificate Object.
	 *
	 * @return the resolved Certificate Object
	 *
	 * @throws java.io.ObjectStreamException if the Certificate
	 *      could not be resolved
	 */
	protected Object readResolve() throws java.io.ObjectStreamException {
	    try {
		CertificateFactory cf = CertificateFactory.getInstance(type);
		return cf.generateCertificate(new java.io.ByteArrayInputStream(data));
	    } catch (CertificateException e) {
		throw new java.io.NotSerializableException(
			"java.security.cert.Certificate: " + type + ": " + e.getMessage());
	    }
	}

	private String type;
	private byte[] data;

    }

}

