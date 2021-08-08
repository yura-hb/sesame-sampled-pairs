import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

class MainFrameModel {
    /**
     * Open file and load the file.
     * @param file the file to open.
     * @throws CheckstyleException if the file can not be parsed.
     */
    public void openFile(File file) throws CheckstyleException {
	if (file != null) {
	    try {
		currentFile = file;
		title = "Checkstyle GUI : " + file.getName();
		reloadActionEnabled = true;
		final DetailAST parseTree;

		switch (parseMode) {
		case PLAIN_JAVA:
		    parseTree = JavaParser.parseFile(file, JavaParser.Options.WITHOUT_COMMENTS);
		    break;
		case JAVA_WITH_COMMENTS:
		case JAVA_WITH_JAVADOC_AND_COMMENTS:
		    parseTree = JavaParser.parseFile(file, JavaParser.Options.WITH_COMMENTS);
		    break;
		default:
		    throw new IllegalArgumentException("Unknown mode: " + parseMode);
		}

		parseTreeTableModel.setParseTree(parseTree);
		parseTreeTableModel.setParseMode(parseMode);
		final String[] sourceLines = getFileText(file).toLinesArray();

		final List&lt;Integer&gt; linesToPositionTemp = new ArrayList&lt;&gt;();
		// starts line counting at 1
		linesToPositionTemp.add(0);

		final StringBuilder sb = new StringBuilder(1024);
		// insert the contents of the file to the text area
		for (final String element : sourceLines) {
		    linesToPositionTemp.add(sb.length());
		    sb.append(element).append(System.lineSeparator());
		}
		linesToPosition = linesToPositionTemp;
		text = sb.toString();
	    } catch (IOException ex) {
		final String exceptionMsg = String.format(Locale.ROOT, "%s occurred while opening file %s.",
			ex.getClass().getSimpleName(), file.getPath());
		throw new CheckstyleException(exceptionMsg, ex);
	    }
	}
    }

    /** The file which is being parsed. */
    private File currentFile;
    /** Title for the main frame. */
    private String title = "Checkstyle GUI";
    /** Whether the reload action is enabled. */
    private boolean reloadActionEnabled;
    /** Current mode. */
    private ParseMode parseMode = ParseMode.PLAIN_JAVA;
    /** Parse tree model. */
    private final ParseTreeTableModel parseTreeTableModel;
    /** Lines to position map. */
    private List&lt;Integer&gt; linesToPosition = new ArrayList&lt;&gt;();
    /** Text for a frame's text area. */
    private String text;

    /**
     * Get FileText from a file.
     * @param file the file to get the FileText from.
     * @return the FileText.
     * @throws IOException if the file could not be read.
     */
    private static FileText getFileText(File file) throws IOException {
	return new FileText(file.getAbsoluteFile(), System.getProperty("file.encoding", StandardCharsets.UTF_8.name()));
    }

}

