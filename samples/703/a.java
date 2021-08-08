class TarEntry implements TarConstants {
    /**
     * Parse an entry's header information from a header buffer.
     *
     * @param header The tar entry header buffer to get information from.
     */
    public void parseTarHeader(byte[] header) {
	int offset = 0;

	name = TarUtils.parseName(header, offset, NAMELEN);
	offset += NAMELEN;
	mode = (int) TarUtils.parseOctal(header, offset, MODELEN);
	offset += MODELEN;
	userId = (int) TarUtils.parseOctal(header, offset, UIDLEN);
	offset += UIDLEN;
	groupId = (int) TarUtils.parseOctal(header, offset, GIDLEN);
	offset += GIDLEN;
	size = TarUtils.parseOctal(header, offset, SIZELEN);
	offset += SIZELEN;
	modTime = TarUtils.parseOctal(header, offset, MODTIMELEN);
	offset += MODTIMELEN;
	offset += CHKSUMLEN;
	linkFlag = header[offset++];
	linkName = TarUtils.parseName(header, offset, NAMELEN);
	offset += NAMELEN;
	magic = TarUtils.parseName(header, offset, MAGICLEN);
	offset += MAGICLEN;
	userName = TarUtils.parseName(header, offset, UNAMELEN);
	offset += UNAMELEN;
	groupName = TarUtils.parseName(header, offset, GNAMELEN);
	offset += GNAMELEN;
	devMajor = (int) TarUtils.parseOctal(header, offset, DEVLEN);
	offset += DEVLEN;
	devMinor = (int) TarUtils.parseOctal(header, offset, DEVLEN);
    }

    /** The entry's name. */
    private StringBuffer name;
    /** The entry's permission mode. */
    private int mode;
    /** The entry's user id. */
    private int userId;
    /** The entry's group id. */
    private int groupId;
    /** The entry's size. */
    private long size;
    /** The entry's modification time. */
    private long modTime;
    /** The entry's link flag. */
    private byte linkFlag;
    /** The entry's link name. */
    private StringBuffer linkName;
    /** The entry's magic tag. */
    private StringBuffer magic;
    /** The entry's user name. */
    private StringBuffer userName;
    /** The entry's group name. */
    private StringBuffer groupName;
    /** The entry's major device number. */
    private int devMajor;
    /** The entry's minor device number. */
    private int devMinor;

}

