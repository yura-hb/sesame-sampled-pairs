import java.io.DataOutputStream;

abstract class CommunicationBase extends TerminateableThread {
    /**
     * @param pMessage
     * @return true, if successful.
     */
    public synchronized boolean send(CollaborationActionBase pCommand) {
	try {
	    printCommand("Send", pCommand);
	    final String marshalledText = Tools.marshall(pCommand);
	    logger.fine(getName() + " :Sending " + marshalledText);
	    String text = Tools.compress(marshalledText);
	    // split into pieces, as the writeUTF method is only able to send
	    // 65535 bytes...
	    int index = 0;
	    while (index + MAX_STRING_LENGTH_TO_SEND &lt; text.length()) {
		out.writeUTF(text.substring(index, index + MAX_STRING_LENGTH_TO_SEND) + STRING_CONTINUATION_SUFFIX);
		index += MAX_STRING_LENGTH_TO_SEND;
	    }
	    out.writeUTF(text.substring(index));
	    return true;
	} catch (IOException e) {
	    freemind.main.Resources.getInstance().logException(e);
	}
	return false;
    }

    private static final int MAX_STRING_LENGTH_TO_SEND = 65500;
    protected DataOutputStream out;
    /**
     * 
     */
    private static final String STRING_CONTINUATION_SUFFIX = "&lt;cont&gt;";

    /**
     * @param pDirection
     * @param pCommand
     */
    private void printCommand(String pDirection, CollaborationActionBase pCommand) {
	if (pCommand instanceof CollaborationTransaction) {
	    CollaborationTransaction trans = (CollaborationTransaction) pCommand;
	    XmlAction doAction = Tools.unMarshall(trans.getDoAction());
	    String out = pDirection + ": " + Tools.printXmlAction(doAction) + " (Id: " + trans.getId() + ")";
	    logger.info(getName() + ":" + out);
	} else {
	    String out = pDirection + ": " + Tools.printXmlAction(pCommand);
	    logger.info(getName() + ":" + out);

	}
    }

}

