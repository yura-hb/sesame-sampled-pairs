import java.io.*;
import java.net.*;

class TargetInterface {
    /**
    * Returns the result of the evaluation sent previously to the target.
    */
    public Result getResult() {
	boolean hasValue = false;
	String typeName = null;
	String toString = null;
	if (DEBUG) {
	    hasValue = true;
	    typeName = "TargetInterface in debug mode. Run d:\\eval\\TestCodeSnippet.bat d:\\eval\\snippets\\"
		    + this.codeSnippetClassName;
	    toString = "";
	} else {
	    if (isConnected()) {
		// TBD: Read type name and toString as a character array
		try {
		    DataInputStream in = new DataInputStream(this.socket.getInputStream());
		    hasValue = in.readBoolean();
		    if (hasValue) {
			typeName = in.readUTF();
			toString = in.readUTF();
		    } else {
			typeName = null;
			toString = null;
		    }
		} catch (IOException e) {
		    // The socket has likely been closed on the other end. So the code snippet runner has stopped.
		    hasValue = true;
		    typeName = e.getMessage();
		    toString = "";
		    disconnect();
		}
	    } else {
		hasValue = true;
		typeName = "Connection has been lost";
		toString = "";
	    }
	}
	if (TIMING) {
	    System.out.println("Time to send compiled classes, run on target and get result is "
		    + (System.currentTimeMillis() - this.sentTime) + "ms");
	}
	Result result = new Result();
	result.displayString = toString == null ? null : toString.toCharArray();
	result.typeName = typeName == null ? null : typeName.toCharArray();
	return result;
    }

    /**
     * Whether class files should be written in d:\eval\ instead of sending them to the target
     * NB: d:\eval should contain a batch file TestCodeSnippet.bat with the following contents:
     *		SET JDK=c:\jdk1.2.2
     *		SET EVAL=d:\eval
     *		%JDK%\bin\java -Xbootclasspath:%JDK%\jre\lib\rt.jar;%EVAL%\javaClasses; -classpath c:\temp;%EVAL%\snippets;%EVAL%\classes;"d:\ide\project_resources\Eclipse Java Evaluation\CodeSnippetSupport.jar" CodeSnippetTester %1
     */
    static final boolean DEBUG = false;
    String codeSnippetClassName;
    /**
     * The connection to the target's ide interface.
     */
    Socket socket;
    /**
     * Whether timing info should be printed to stdout
     */
    static final boolean TIMING = false;
    long sentTime;

    /**
    * Returns whether this interface is connected to the target.
    */
    boolean isConnected() {
	return this.socket != null;
    }

    /**
    * (PRIVATE API)
    * Disconnects this interface from the target.
    */
    public void disconnect() {
	if (this.socket != null) {
	    try {
		this.socket.close();
	    } catch (IOException e) {
		// Already closed. Nothing more to do
	    }
	    this.socket = null;
	}
    }

}

