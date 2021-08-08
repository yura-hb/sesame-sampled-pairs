import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;

class MidiUtils {
    /** return if the given message is a meta tempo message */
    public static boolean isMetaTempo(MidiMessage midiMsg) {
	// first check if it is a META message at all
	if (midiMsg.getLength() != 6 || midiMsg.getStatus() != MetaMessage.META) {
	    return false;
	}
	// now get message and check for tempo
	byte[] msg = midiMsg.getMessage();
	// meta type must be 0x51, and data length must be 3
	return ((msg[1] & 0xFF) == META_TEMPO_TYPE) && (msg[2] == 3);
    }

    public static final int META_TEMPO_TYPE = 0x51;

}

