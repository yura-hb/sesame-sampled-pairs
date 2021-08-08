import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

class WSL_Util {
    /**
     * This method copies a source file to a target file
     * @param srcFile
     * @param destFile
     * @throws IOException
     */
    public void copyFile(File srcFile, File destFile) throws IOException {
	InputStream oInStream = new FileInputStream(srcFile);
	OutputStream oOutStream = new FileOutputStream(destFile); //here
	// Transfer bytes from in to out
	byte[] oBytes = new byte[1024];
	int nLength;
	BufferedInputStream oBuffInputStream = new BufferedInputStream(oInStream);
	while ((nLength = oBuffInputStream.read(oBytes)) &gt; 0) {
	    oOutStream.write(oBytes, 0, nLength);
	}
	oInStream.close();
	oOutStream.close();
    }

}

