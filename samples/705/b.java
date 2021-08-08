import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.ProfileDeferralMgr;

class ICC_Profile implements Serializable {
    /**
     * Returns profile minor version.
     * @return The minor version of the profile.
     */
    public int getMinorVersion() {
	byte[] theHeader;

	theHeader = getData(icSigHead); /* getData will activate deferred
					   profiles if necessary */

	return (int) theHeader[9];
    }

    /**
     * ICC Profile Tag Signature: 'head' - special.
     */
    public static final int icSigHead = 0x68656164;
    private transient Profile cmmProfile;

    /**
     * Returns a particular tagged data element from the profile as
     * a byte array.  Elements are identified by signatures
     * as defined in the ICC specification.  The signature
     * icSigHead can be used to get the header.  This method is useful
     * for advanced applets or applications which need to access
     * profile data directly.
     *
     * @param tagSignature The ICC tag signature for the data element you
     * want to get.
     *
     * @return A byte array that contains the tagged data element. Returns
     * {@code null} if the specified tag doesn't exist.
     * @see #setData(int, byte[])
     */
    public byte[] getData(int tagSignature) {

	if (ProfileDeferralMgr.deferring) {
	    ProfileDeferralMgr.activateProfiles();
	}

	return getData(cmmProfile, tagSignature);
    }

    static byte[] getData(Profile p, int tagSignature) {
	int tagSize;
	byte[] tagData;

	try {
	    PCMM mdl = CMSManager.getModule();

	    /* get the number of bytes needed for this tag */
	    tagSize = mdl.getTagSize(p, tagSignature);

	    tagData = new byte[tagSize]; /* get an array for the tag */

	    /* get the tag's data */
	    mdl.getTagData(p, tagSignature, tagData);
	} catch (CMMException c) {
	    tagData = null;
	}

	return tagData;
    }

}

