class GroupEntry extends BaseEntry {
    /**
     * Resets the group entry to its initial state.
     */
    public void reset() {
	isInstantMatch = false;
	rewriteMatch = null;
	longestRewriteMatch = 0;
	suffixMatch = null;
	longestSuffixMatch = 0;
	systemEntrySearched = false;
    }

    boolean isInstantMatch = false;
    String rewriteMatch = null;
    int longestRewriteMatch = 0;
    String suffixMatch = null;
    int longestSuffixMatch = 0;
    boolean systemEntrySearched = false;

}

