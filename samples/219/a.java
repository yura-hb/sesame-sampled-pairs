class SelectionRequestor implements ISelectionRequestor {
    /**
    * Returns the resolved elements.
    */
    public IJavaElement[] getElements() {
	int elementLength = this.elementIndex + 1;
	if (this.elements.length != elementLength) {
	    System.arraycopy(this.elements, 0, this.elements = new IJavaElement[elementLength], 0, elementLength);
	}
	return this.elements;
    }

    protected int elementIndex = -1;
    protected IJavaElement[] elements = JavaElement.NO_ELEMENTS;

}

