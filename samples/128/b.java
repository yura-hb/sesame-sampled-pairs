import java.io.Reader;
import java.io.CharArrayReader;
import java.io.FilterReader;
import java.util.Stack;

class DTDInputStream extends FilterReader implements DTDConstants {
    /**
     * Push an array of bytes.
     */
    public void push(char data[]) throws IOException {
	if (data.length &gt; 0) {
	    push(new CharArrayReader(data));
	}
    }

    public Stack&lt;Object&gt; stack = new Stack&lt;&gt;();
    public int ln = 1;
    public int ch;

    /**
     * Push an entire input stream
     */
    void push(Reader in) throws IOException {
	stack.push(new Integer(ln));
	stack.push(new Integer(ch));
	stack.push(this.in);
	this.in = in;
	ch = in.read();
    }

}

