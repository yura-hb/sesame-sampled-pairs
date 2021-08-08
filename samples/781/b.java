import java.security.cert.*;
import java.util.Date;

class CertificateValidity implements CertAttrSet&lt;String&gt; {
    /**
     * Verify that the current time is within the validity period.
     *
     * @exception CertificateExpiredException if the certificate has expired.
     * @exception CertificateNotYetValidException if the certificate is not
     * yet valid.
     */
    public void valid() throws CertificateNotYetValidException, CertificateExpiredException {
	Date now = new Date();
	valid(now);
    }

    private Date notBefore;
    private Date notAfter;

    /**
     * Verify that the passed time is within the validity period.
     * @param now the Date against which to compare the validity
     * period.
     *
     * @exception CertificateExpiredException if the certificate has expired
     * with respect to the &lt;code&gt;Date&lt;/code&gt; supplied.
     * @exception CertificateNotYetValidException if the certificate is not
     * yet valid with respect to the &lt;code&gt;Date&lt;/code&gt; supplied.
     *
     */
    public void valid(Date now) throws CertificateNotYetValidException, CertificateExpiredException {
	/*
	 * we use the internal Dates rather than the passed in Date
	 * because someone could override the Date methods after()
	 * and before() to do something entirely different.
	 */
	if (notBefore.after(now)) {
	    throw new CertificateNotYetValidException("NotBefore: " + notBefore.toString());
	}
	if (notAfter.before(now)) {
	    throw new CertificateExpiredException("NotAfter: " + notAfter.toString());
	}
    }

}

