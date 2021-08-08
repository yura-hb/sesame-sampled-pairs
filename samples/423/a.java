import java.util.regex.Pattern;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

class RegexpCheck extends AbstractCheck {
    /**
     * Set the format to the specified regular expression.
     * @param pattern the new pattern
     * @throws org.apache.commons.beanutils.ConversionException unable to parse format
     */
    public final void setFormat(Pattern pattern) {
	format = CommonUtil.createPattern(pattern.pattern(), Pattern.MULTILINE);
    }

    /** The regexp to match against. */
    private Pattern format = Pattern.compile("^$", Pattern.MULTILINE);

}

