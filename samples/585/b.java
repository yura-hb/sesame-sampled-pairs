import nsk.share.*;
import nsk.share.jpda.*;
import java.util.*;

abstract class Debugee extends DebugeeProcess {
    /**
     * Find line index for the given line number from the method line table.
     */
    public long getCodeIndex(long classID, long methodID, int lineNumber) {
	String commandName = "Method.LineTable";
	CommandPacket command = new CommandPacket(JDWP.Command.Method.LineTable);
	command.addReferenceTypeID(classID);
	command.addMethodID(methodID);
	ReplyPacket reply = receiveReplyFor(command, commandName);
	String msg = "Error occured while getting code index for line number: " + lineNumber;
	try {
	    reply.resetPosition();
	    long start = reply.getLong();
	    long end = reply.getLong();
	    int lines = reply.getInt();
	    for (int i = 0; i &lt; lines; i++) {
		long lineCodeIndex = reply.getLong();
		int line = reply.getInt();
		if (lineNumber == line) {
		    return lineCodeIndex;
		}
	    }
	} catch (BoundException e) {
	    complain("Unable to parse reply packet for " + commandName + " command:\n\t" + e.getMessage());
	    display("Reply packet:\n" + reply);
	    throw new Failure(msg);
	}

	throw new Failure("No code index found for line number: " + lineNumber);
    }

    protected Transport transport = null;
    protected LinkedList&lt;EventPacket&gt; eventQueue = new LinkedList&lt;EventPacket&gt;();

    /**
     * Send specified command packet, receive and check reply packet.
     *
     * @throws Failure if exception caught in sending and reading packets
     */
    public ReplyPacket receiveReplyFor(CommandPacket command, String commandName) {
	ReplyPacket reply = null;
	sendCommand(command, commandName);
	reply = receiveReply();
	try {
	    reply.checkHeader(command.getPacketID());
	} catch (BoundException e) {
	    complain("Wrong header of reply packet for command " + commandName + ":\n\t" + e.getMessage());
	    display("Reply packet:\n" + reply);
	    throw new Failure("Wrong reply packet received for command: " + commandName);
	}
	return reply;
    }

    /**
     * Sends JDWP command packet.
     */
    public void sendCommand(CommandPacket packet, String commandName) {
	try {
	    transport.write(packet);
	} catch (IOException e) {
	    e.printStackTrace(log.getOutStream());
	    complain("Caught IOException while sending command packet for " + commandName + ":\n\t" + e);
	    display("Command packet:\n" + packet);
	    throw new Failure("Error occured while sending command: " + commandName);
	}
    }

    /**
     * Receive next JDWP reply packet.
     */
    public ReplyPacket receiveReply() {
	try {
	    for (;;) {
		Packet packet = new Packet();
		transport.read(packet);

		if (packet.getFlags() == JDWP.Flag.REPLY_PACKET) {
		    ReplyPacket reply = new ReplyPacket(packet);
		    return reply;
		}

		EventPacket event = new EventPacket(packet);
		display("Placing received event packet into queue");
		eventQueue.add(event);
	    }
	} catch (IOException e) {
	    e.printStackTrace(log.getOutStream());
	    throw new Failure("Caught IOException while receiving reply packet:\n\t" + e);
	}
    }

}

