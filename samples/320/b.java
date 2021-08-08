import java.io.*;
import java.util.*;
import java.security.*;
import sun.net.www.MessageHeader;
import java.util.Base64;

class SignatureFile {
    /**
     * Add a specific entry from the current manifest.
     */
    public void add(String entry) throws JarException {
	MessageHeader mh = manifest.getEntry(entry);
	if (mh == null) {
	    throw new JarException("entry " + entry + " not in manifest");
	}
	MessageHeader smh;
	try {
	    smh = computeEntry(mh);
	} catch (IOException e) {
	    throw new JarException(e.getMessage());
	}
	entries.addElement(smh);
    }

    private Manifest manifest;
    private Vector&lt;MessageHeader&gt; entries = new Vector&lt;&gt;();
    static final String[] hashes = { "SHA" };
    private Hashtable&lt;String, MessageDigest&gt; digests = new Hashtable&lt;&gt;();

    /**
     * Given a manifest entry, computes the signature entry for this
     * manifest entry.
     */
    private MessageHeader computeEntry(MessageHeader mh) throws IOException {
	MessageHeader smh = new MessageHeader();

	String name = mh.findValue("Name");
	if (name == null) {
	    return null;
	}
	smh.set("Name", name);

	try {
	    for (int i = 0; i &lt; hashes.length; ++i) {
		MessageDigest dig = getDigest(hashes[i]);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		mh.print(ps);
		byte[] headerBytes = baos.toByteArray();
		byte[] digest = dig.digest(headerBytes);
		smh.set(hashes[i] + "-Digest", Base64.getMimeEncoder().encodeToString(digest));
	    }
	    return smh;
	} catch (NoSuchAlgorithmException e) {
	    throw new JarException(e.getMessage());
	}
    }

    private MessageDigest getDigest(String algorithm) throws NoSuchAlgorithmException {
	MessageDigest dig = digests.get(algorithm);
	if (dig == null) {
	    dig = MessageDigest.getInstance(algorithm);
	    digests.put(algorithm, dig);
	}
	dig.reset();
	return dig;
    }

}

