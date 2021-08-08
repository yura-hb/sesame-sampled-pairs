import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

class WriteTagCheck extends AbstractCheck {
    /**
     * Sets the tag to check.
     * @param tag tag to check
     */
    public void setTag(String tag) {
	this.tag = tag;
	tagRegExp = CommonUtil.createPattern(tag + "\\s*(.*$)");
    }

    /** Regexp to match tag. */
    private String tag;
    /** Compiled regexp to match tag. **/
    private Pattern tagRegExp;

}

