import org.nd4j.linalg.api.ndarray.*;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.string.NDArrayStrings;
import java.io.*;
import java.util.*;

class Nd4j {
    /**
     * Write ndarray as text to output stream
     * @param write
     * @param os
     */
    public static void writeTxtString(INDArray write, OutputStream os) {
	try {
	    // default format is "0.000000000000000000E0"
	    String toWrite = writeStringForArray(write, "0.000000000000000000E0");
	    os.write(toWrite.getBytes());
	} catch (IOException e) {
	    throw new RuntimeException("Error writing output", e);
	}
    }

    private static String writeStringForArray(INDArray write, String format) {
	if (write.isView() || !Shape.hasDefaultStridesForShape(write))
	    write = write.dup();
	if (format.isEmpty())
	    format = "0.000000000000000000E0";
	String lineOne = "{\n";
	String lineTwo = "\"filefrom\": \"dl4j\",\n";
	String lineThree = "\"ordering\": \"" + write.ordering() + "\",\n";
	String lineFour = "\"shape\":\t" + java.util.Arrays.toString(write.shape()) + ",\n";
	String lineFive = "\"data\":\n";
	String fileData = new NDArrayStrings(",", format).format(write, false);
	String fileEnd = "\n}\n";
	String fileBegin = lineOne + lineTwo + lineThree + lineFour + lineFive;
	String fileContents = fileBegin + fileData + fileEnd;
	return fileContents;
    }

}

