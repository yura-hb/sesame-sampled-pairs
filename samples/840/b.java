import java.security.cert.*;
import sun.security.util.*;

class X509CertInfo implements CertAttrSet&lt;String&gt; {
    /**
     * Returns the encoded certificate info.
     *
     * @exception CertificateEncodingException on encoding information errors.
     */
    public byte[] getEncodedInfo() throws CertificateEncodingException {
	try {
	    if (rawCertInfo == null) {
		DerOutputStream tmp = new DerOutputStream();
		emit(tmp);
		rawCertInfo = tmp.toByteArray();
	    }
	    return rawCertInfo.clone();
	} catch (IOException e) {
	    throw new CertificateEncodingException(e.toString());
	} catch (CertificateException e) {
	    throw new CertificateEncodingException(e.toString());
	}
    }

    private byte[] rawCertInfo = null;
    protected CertificateVersion version = new CertificateVersion();
    protected CertificateSerialNumber serialNum = null;
    protected CertificateAlgorithmId algId = null;
    protected X500Name issuer = null;
    protected CertificateValidity interval = null;
    protected X500Name subject = null;
    protected CertificateX509Key pubKey = null;
    protected UniqueIdentity issuerUniqueId = null;
    protected UniqueIdentity subjectUniqueId = null;
    protected CertificateExtensions extensions = null;

    private void emit(DerOutputStream out) throws CertificateException, IOException {
	DerOutputStream tmp = new DerOutputStream();

	// version number, iff not V1
	version.encode(tmp);

	// Encode serial number, issuer signing algorithm, issuer name
	// and validity
	serialNum.encode(tmp);
	algId.encode(tmp);

	if ((version.compare(CertificateVersion.V1) == 0) && (issuer.toString() == null))
	    throw new CertificateParsingException("Null issuer DN not allowed in v1 certificate");

	issuer.encode(tmp);
	interval.encode(tmp);

	// Encode subject (principal) and associated key
	if ((version.compare(CertificateVersion.V1) == 0) && (subject.toString() == null))
	    throw new CertificateParsingException("Null subject DN not allowed in v1 certificate");
	subject.encode(tmp);
	pubKey.encode(tmp);

	// Encode issuerUniqueId & subjectUniqueId.
	if (issuerUniqueId != null) {
	    issuerUniqueId.encode(tmp, DerValue.createTag(DerValue.TAG_CONTEXT, false, (byte) 1));
	}
	if (subjectUniqueId != null) {
	    subjectUniqueId.encode(tmp, DerValue.createTag(DerValue.TAG_CONTEXT, false, (byte) 2));
	}

	// Write all the extensions.
	if (extensions != null) {
	    extensions.encode(tmp);
	}

	// Wrap the data; encoding of the "raw" cert is now complete.
	out.write(DerValue.tag_Sequence, tmp);
    }

}

