class BitScanReverseNode extends UnaryNode implements ArithmeticLIRLowerable {
    /**
     * Utility method with defined return value for 0.
     *
     * @param v
     * @return index of first set bit or -1 if {@code v} == 0.
     */
    public static int scan(int v) {
	return 31 - Integer.numberOfLeadingZeros(v);
    }

}

