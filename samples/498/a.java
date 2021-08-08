import java.io.InputStream;
import java.io.Reader;
import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.eclipse.jdt.apt.pluggable.tests.ProcessorTestStatus;

class FilerTesterProc extends AbstractProcessor {
    /**
     * Attempt to get an existing resource from the SOURCE_OUTPUT.
     */
    public void testGetResource01(Element e, String arg0, String arg1) throws Exception {
	FileObject resource = _filer.getResource(StandardLocation.SOURCE_OUTPUT, arg0, arg1);
	checkResourceContents01(resource, resource01Name, resource01FileContents);
    }

    private Filer _filer;
    public static final String resource01Name = ".apt_generated/g/Test.java";
    public static final String resource01FileContents = "package g;\n" + "public class Test {}\n";

    /**
     * Check that the resource can be opened, examined, and its contents match
     * {@link #checkResourceContents01(FileObject)}getResource01FileContents
     */
    private void checkResourceContents01(FileObject resource, String expectedName, String expectedContents)
	    throws Exception {

	long modTime = resource.getLastModified();
	if (modTime &lt;= 0) {
	    ProcessorTestStatus.fail("resource had unexpected mod time: " + modTime);
	}

	String actualName = resource.getName();
	if (!expectedName.equals(actualName)) {
	    System.out
		    .println("Resource had unexpected name.  Expected " + expectedName + ", actual was " + actualName);
	    ProcessorTestStatus.fail("Resource had unexpected name");
	}

	InputStream stream = resource.openInputStream();
	if (stream.available() &lt;= 0) {
	    ProcessorTestStatus.fail("stream contained no data");
	}
	byte actualBytes[] = new byte[512];
	int length = stream.read(actualBytes);
	String actualStringContents = new String(actualBytes, 0, length);
	if (!expectedContents.equals(actualStringContents)) {
	    System.out.println("Expected stream contents:\n" + expectedContents);
	    System.out.println("Actual contents were:\n" + actualStringContents);
	    ProcessorTestStatus.fail("stream did not contain expected contents");
	}
	stream.close();

	char actualChars[] = new char[512];
	Reader reader = resource.openReader(true);
	length = reader.read(actualChars, 0, actualChars.length);
	actualStringContents = new String(actualChars, 0, length);
	if (!expectedContents.equals(actualStringContents)) {
	    System.out.println("Expected reader contents:\n" + expectedContents);
	    System.out.println("Actual contents were:\n" + actualStringContents);
	    ProcessorTestStatus.fail("reader did not contain expected contents");
	}
	reader.close();

	CharSequence actualCharContent = resource.getCharContent(true);
	if (!expectedContents.equals(actualCharContent.toString())) {
	    System.out.println("Expected getCharContent to return:\n" + expectedContents);
	    System.out.println("Actual getCharContent returned:\n" + actualCharContent);
	    ProcessorTestStatus.fail("getCharContent() did not return expected contents");
	}
    }

}

