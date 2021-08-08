class Flags {
    /**
     * Returns whether the given integer includes the &lt;code&gt;final&lt;/code&gt; modifier.
     *
     * @param flags the flags
     * @return &lt;code&gt;true&lt;/code&gt; if the &lt;code&gt;final&lt;/code&gt; modifier is included
     */
    public static boolean isFinal(int flags) {
	return (flags & AccFinal) != 0;
    }

    /**
     * Final access flag. See The Java Virtual Machine Specification for more details.
     * @since 2.0
     */
    public static final int AccFinal = ClassFileConstants.AccFinal;

}

