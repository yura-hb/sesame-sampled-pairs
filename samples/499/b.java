class Token {
    /**
     * Static hash code computation function token
     *
     * @param token a token
     *
     * @return hash code for token
     */
    public static int hashCode(final long token) {
	return (int) (token ^ token &gt;&gt;&gt; 32);
    }

}

