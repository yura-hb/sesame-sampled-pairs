class MeshOrganizer implements Serializable {
    /**
     * This methods adds new node to the network
     */
    public Node addNode(@NonNull String ip, @NonNull int port) {
	val node = Node.builder().id(ip).port(port).upstream(null).build();

	return this.addNode(node);
    }

    /**
     * This method adds new node to the network
     *
     * PLEASE NOTE: Default port 40123 is used
     * @param ip
     */
    public Node addNode(@NonNull String ip) {
	return addNode(ip, 40123);
    }

}

