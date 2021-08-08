import java.security.cert.CRLException;
import java.util.*;
import sun.security.util.*;

class X509CRLEntryImpl extends X509CRLEntry implements Comparable&lt;X509CRLEntryImpl&gt; {
    /**
     * Encodes the revoked certificate to an output stream.
     *
     * @param outStrm an output stream to which the encoded revoked
     * certificate is written.
     * @exception CRLException on encoding errors.
     */
    public void encode(DerOutputStream outStrm) throws CRLException {
	try {
	    if (revokedCert == null) {
		DerOutputStream tmp = new DerOutputStream();
		// sequence { serialNumber, revocationDate, extensions }
		serialNumber.encode(tmp);

		if (revocationDate.getTime() &lt; YR_2050) {
		    tmp.putUTCTime(revocationDate);
		} else {
		    tmp.putGeneralizedTime(revocationDate);
		}

		if (extensions != null)
		    extensions.encode(tmp, isExplicit);

		DerOutputStream seq = new DerOutputStream();
		seq.write(DerValue.tag_Sequence, tmp);

		revokedCert = seq.toByteArray();
	    }
	    outStrm.write(revokedCert);
	} catch (IOException e) {
	    throw new CRLException("Encoding error: " + e.toString());
	}
    }

    private byte[] revokedCert = null;
    private SerialNumber serialNumber = null;
    private Date revocationDate = null;
    private static final long YR_2050 = 2524636800000L;
    private CRLExtensions extensions = null;
    private static final boolean isExplicit = false;

}

