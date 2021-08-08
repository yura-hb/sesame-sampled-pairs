class ExampleFileFilter extends FileFilter {
    /**
     * Sets the human readable description of this filter. For example:
     * filter.setDescription("Gif and JPG Images");
     * 
     */
    public void setDescription(String description) {
	this.description = description;
	fullDescription = null;
    }

    private String description = null;
    private String fullDescription = null;

}

