class RegionFactory&lt;S&gt; {
    /** Compute the union of two regions.
     * @param region1 first region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @param region2 second region (will be unusable after the operation as
     * parts of it will be reused in the new region)
     * @return a new region, result of {@code region1 union region2}
     */
    public Region&lt;S&gt; union(final Region&lt;S&gt; region1, final Region&lt;S&gt; region2) {
	final BSPTree&lt;S&gt; tree = region1.getTree(false).merge(region2.getTree(false), new UnionMerger());
	tree.visit(nodeCleaner);
	return region1.buildNew(tree);
    }

    /** Visitor removing internal nodes attributes. */
    private final NodesCleaner nodeCleaner;

}

