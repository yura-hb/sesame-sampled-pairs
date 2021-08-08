class SubscriberState implements Serializable, Comparable&lt;SubscriberState&gt; {
    /**
     * Return the server opType (master or slave)
     * @return the server opType
     */
    public String serverType() {
	return isMaster ? "master" : "slave";
    }

    private boolean isMaster;

}

