import nsk.share.*;
import nsk.share.jpda.*;
import java.util.*;

abstract class Debugee extends DebugeeProcess {
    /**
     * Find threadID for given thread name among all active threads.
     */
    public long getThreadID(String name) {
	// request list of all threadIDs
	int threads = 0;
	long threadIDs[] = null;
	{
	    String commandName = "VirtualMachine.AllThreads";
	    CommandPacket command = new CommandPacket(JDWP.Command.VirtualMachine.AllThreads);
	    ReplyPacket reply = receiveReplyFor(command, commandName);
	    reply.resetPosition();
	    try {
		threads = reply.getInt();
		threadIDs = new long[threads];

		for (int i = 0; i &lt; threads; i++) {
		    threadIDs[i] = reply.getObjectID();
		}
	    } catch (BoundException e) {
		complain("Unable to parse reply packet for " + commandName + " command:\n\t" + e.getMessage());
		display("Reply packet:\n" + reply);
		throw new Failure("Error occured while getting threadID for thread name: " + name);
	    }
	}

	// request name for each threadID
	for (int i = 0; i &lt; threads; i++) {
	    String commandName = "ThreadReference.Name";
	    CommandPacket command = new CommandPacket(JDWP.Command.ThreadReference.Name);
	    command.addObjectID(threadIDs[i]);
	    ReplyPacket reply = receiveReplyFor(command, commandName);
	    try {
		reply.resetPosition();
		String threadName = reply.getString();
		if (threadName.equals(name)) {
		    return threadIDs[i];
		}
	    } catch (BoundException e) {
		complain("Unable to parse reply packet for " + commandName + " command:\n\t" + e.getMessage());
		display("Reply packet:\n" + reply);
		throw new Failure("Error occured while getting name for threadID: " + threadIDs[i]);
	    }
	}

	throw new Failure("No threadID found for thread name: " + name);
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

