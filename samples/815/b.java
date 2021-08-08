class MemberBuilder&lt;S, T, E, M&gt; extends DeclBuilder&lt;S, T, E, M&gt; {
    /**
     * Build the member.
     *
     * @return a byte array representation of the member
     */
    protected byte[] build() {
	GrowableByteBuffer buf = new GrowableByteBuffer();
	addAnnotations();
	build(buf);
	return buf.bytes();
    }

    CharSequence name;
    T desc;

    /**
     * Build the member.
     *
     * @param buf the {@code GrowableByteBuffer} to build the member into
     */
    protected void build(GrowableByteBuffer buf) {
	addAnnotations();
	buf.writeChar(flags);
	buf.writeChar(poolHelper.putUtf8(name));
	buf.writeChar(poolHelper.putType(desc));
	buf.writeChar(nattrs);
	buf.writeBytes(attributes);
    }

}

