import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.ProfileDeferralMgr;
import sun.java2d.cmm.ProfileDeferralInfo;

class ICC_Profile implements Serializable {
    /**
     * Returns the profile class.
     * @return One of the predefined profile class constants.
     */
    public int getProfileClass() {
	byte[] theHeader;
	int theClassSig, theClass;

	if (deferralInfo != null) {
	    return deferralInfo.profileClass; /* Need to have this info for
					         ICC_ColorSpace without
					         causing a deferred profile
					         to be loaded */
	}

	theHeader = getData(icSigHead);

	theClassSig = intFromBigEndian(theHeader, icHdrDeviceClass);

	switch (theClassSig) {
	case icSigInputClass:
	    theClass = CLASS_INPUT;
	    break;

	case icSigDisplayClass:
	    theClass = CLASS_DISPLAY;
	    break;

	case icSigOutputClass:
	    theClass = CLASS_OUTPUT;
	    break;

	case icSigLinkClass:
	    theClass = CLASS_DEVICELINK;
	    break;

	case icSigColorSpaceClass:
	    theClass = CLASS_COLORSPACECONVERSION;
	    break;

	case icSigAbstractClass:
	    theClass = CLASS_ABSTRACT;
	    break;

	case icSigNamedColorClass:
	    theClass = CLASS_NAMEDCOLOR;
	    break;

	default:
	    throw new IllegalArgumentException("Unknown profile class");
	}

	return theClass;
    }

    private transient ProfileDeferralInfo deferralInfo;
    /**
     * ICC Profile Tag Signature: 'head' - special.
     */
    public static final int icSigHead = 0x68656164;
    /**
     * ICC Profile Header Location: type of profile.
     */
    public static final int icHdrDeviceClass = 12;
    /**
     * ICC Profile Class Signature: 'scnr'.
     */
    public static final int icSigInputClass = 0x73636E72;
    /**
     * Profile class is input.
     */
    public static final int CLASS_INPUT = 0;
    /**
     * ICC Profile Class Signature: 'mntr'.
     */
    public static final int icSigDisplayClass = 0x6D6E7472;
    /**
     * Profile class is display.
     */
    public static final int CLASS_DISPLAY = 1;
    /**
     * ICC Profile Class Signature: 'prtr'.
     */
    public static final int icSigOutputClass = 0x70727472;
    /**
     * Profile class is output.
     */
    public static final int CLASS_OUTPUT = 2;
    /**
     * ICC Profile Class Signature: 'link'.
     */
    public static final int icSigLinkClass = 0x6C696E6B;
    /**
     * Profile class is device link.
     */
    public static final int CLASS_DEVICELINK = 3;
    /**
     * ICC Profile Class Signature: 'spac'.
     */
    public static final int icSigColorSpaceClass = 0x73706163;
    /**
     * Profile class is color space conversion.
     */
    public static final int CLASS_COLORSPACECONVERSION = 4;
    /**
     * ICC Profile Class Signature: 'abst'.
     */
    public static final int icSigAbstractClass = 0x61627374;
    /**
     * Profile class is abstract.
     */
    public static final int CLASS_ABSTRACT = 5;
    /**
     * ICC Profile Class Signature: 'nmcl'.
     */
    public static final int icSigNamedColorClass = 0x6e6d636c;
    /**
     * Profile class is named color.
     */
    public static final int CLASS_NAMEDCOLOR = 6;
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

    static int intFromBigEndian(byte[] array, int index) {
	return (((array[index] & 0xff) &lt;&lt; 24) | ((array[index + 1] & 0xff) &lt;&lt; 16) | ((array[index + 2] & 0xff) &lt;&lt; 8)
		| (array[index + 3] & 0xff));
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

