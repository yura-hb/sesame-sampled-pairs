class MeshOrganizer implements Serializable {
    class Node implements Serializable, Comparable&lt;Node&gt; {
	/**
	 * This method returns number of hops between
	 * @return
	 */
	public int distanceFromRoot() {
	    if (upstream.isRootNode())
		return 1;
	    else
		return upstream.distanceFromRoot() + 1;
	}

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private Node upstream;

    }

}

