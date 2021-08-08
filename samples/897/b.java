class ZipOutputStream extends DeflaterOutputStream implements ZipConstants {
    /**
     * Sets the ZIP file comment.
     * @param comment the comment string
     * @exception IllegalArgumentException if the length of the specified
     *            ZIP file comment is greater than 0xFFFF bytes
     */
    public void setComment(String comment) {
	if (comment != null) {
	    this.comment = zc.getBytes(comment);
	    if (this.comment.length &gt; 0xffff)
		throw new IllegalArgumentException("ZIP file comment too long.");
	}
    }

    private byte[] comment;
    private final ZipCoder zc;

}

