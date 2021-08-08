import java.io.*;

class T6917288 {
    /**
     *  Record an error message.
     */
    void error(String msg) {
	System.err.println(msg);
	errors++;
    }

    int errors;

}

