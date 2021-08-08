import org.nd4j.base.Preconditions;

abstract class BaseNDArrayFactory implements NDArrayFactory {
    /**
     * Sets the order. Primarily for testing purposes
     *
     * @param order
     */
    @Override
    public void setOrder(char order) {
	Preconditions.checkArgument(order == 'c' || order == 'f', "Order specified must be either c or f: got %s",
		String.valueOf(order));
	this.order = order;
    }

    protected char order;

}

