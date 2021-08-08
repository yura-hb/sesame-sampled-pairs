class XSLTC {
    /**
     * Set the top-level stylesheet
     */
    public void setStylesheet(Stylesheet stylesheet) {
	if (_stylesheet == null)
	    _stylesheet = stylesheet;
    }

    private Stylesheet _stylesheet;

}

