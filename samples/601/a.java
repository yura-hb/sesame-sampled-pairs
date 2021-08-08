import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;

class Hdf5Archive implements Closeable {
    /**
     * Read string attribute from group path.
     *
     * @param attributeName Name of attribute
     * @param groups        Array of zero or more ancestor groups from root to parent.
     * @return HDF5 attribute as String
     * @throws UnsupportedKerasConfigurationException Unsupported Keras config
     */
    public String readAttributeAsString(String attributeName, String... groups)
	    throws UnsupportedKerasConfigurationException {
	if (groups.length == 0) {
	    hdf5.Attribute a = this.file.openAttribute(attributeName);
	    String s = readAttributeAsString(a);
	    a.deallocate();
	    return s;
	}
	hdf5.Group[] groupArray = openGroups(groups);
	hdf5.Attribute a = groupArray[groups.length - 1].openAttribute(attributeName);
	String s = readAttributeAsString(a);
	a.deallocate();
	closeGroups(groupArray);
	return s;
    }

    private hdf5.H5File file;

    /**
     * Read attribute as string.
     *
     * @param attribute HDF5 attribute to read as string.
     * @return HDF5 attribute as string
     * @throws UnsupportedKerasConfigurationException Unsupported Keras config
     */
    private String readAttributeAsString(hdf5.Attribute attribute) throws UnsupportedKerasConfigurationException {
	hdf5.VarLenType vl = attribute.getVarLenType();
	int bufferSizeMult = 1;
	String s = null;
	/* TODO: find a less hacky way to do this.
	 * Reading variable length strings (from attributes) is a giant
	 * pain. There does not appear to be any way to determine the
	 * length of the string in advance, so we use a hack: choose a
	 * buffer size and read the config, increase buffer and repeat
	 * until the buffer ends with \u0000
	 */
	while (true) {
	    byte[] attrBuffer = new byte[bufferSizeMult * 2000];
	    BytePointer attrPointer = new BytePointer(attrBuffer);
	    attribute.read(vl, attrPointer);
	    attrPointer.get(attrBuffer);
	    s = new String(attrBuffer);

	    if (s.endsWith("\u0000")) {
		s = s.replace("\u0000", "");
		break;
	    }

	    bufferSizeMult++;
	    if (bufferSizeMult &gt; 1000) {
		throw new UnsupportedKerasConfigurationException("Could not read abnormally long HDF5 attribute");
	    }
	}
	vl.deallocate();
	return s;
    }

    private hdf5.Group[] openGroups(String... groups) {
	hdf5.Group[] groupArray = new hdf5.Group[groups.length];
	groupArray[0] = this.file.openGroup(groups[0]);
	for (int i = 1; i &lt; groups.length; i++) {
	    groupArray[i] = groupArray[i - 1].openGroup(groups[i]);
	}
	return groupArray;
    }

    private void closeGroups(hdf5.Group[] groupArray) {
	for (int i = groupArray.length - 1; i &gt;= 0; i--) {
	    groupArray[i].deallocate();
	}
    }

}

