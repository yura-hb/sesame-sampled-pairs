class MD5 extends DigestBase {
    /**
     * Reset the state of this object.
     */
    void implReset() {
	// Load magic initialization constants.
	state[0] = 0x67452301;
	state[1] = 0xefcdab89;
	state[2] = 0x98badcfe;
	state[3] = 0x10325476;
    }

    private int[] state;

}

