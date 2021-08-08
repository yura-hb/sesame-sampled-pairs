import java.util.Vector;

class StylePatternFrame extends JPanel implements TextTranslator, PropertyChangeListener {
    /**
     * For the child pattern box, the list is set here.
     * 
     */
    public void setPatternList(List&lt;Pattern&gt; patternList) {
	this.mPatternList = patternList;
	Vector&lt;String&gt; childNames = getPatternNames();
	mChildPattern.updateComboBoxEntries(childNames, childNames);
    }

    private List&lt;Pattern&gt; mPatternList;
    private ComboProperty mChildPattern;

    private Vector&lt;String&gt; getPatternNames() {
	Vector&lt;String&gt; childNames = new Vector&lt;&gt;();
	for (Pattern pattern : mPatternList) {
	    childNames.add(pattern.getName());
	}
	return childNames;
    }

}

