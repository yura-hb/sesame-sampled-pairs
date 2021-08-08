import java.util.Vector;

class Sequence {
    /**
     * Obtains the duration of this sequence, expressed in microseconds.
     *
     * @return this sequence's duration in microseconds
     */
    public long getMicrosecondLength() {

	return com.sun.media.sound.MidiUtils.tick2microsecond(this, getTickLength(), null);
    }

    /**
     * The MIDI tracks in this sequence.
     *
     * @see #getTracks
     */
    protected Vector&lt;Track&gt; tracks = new Vector&lt;&gt;();

    /**
     * Obtains the duration of this sequence, expressed in MIDI ticks.
     *
     * @return this sequence's length in ticks
     * @see #getMicrosecondLength
     */
    public long getTickLength() {

	long length = 0;

	synchronized (tracks) {

	    for (int i = 0; i &lt; tracks.size(); i++) {
		long temp = tracks.elementAt(i).ticks();
		if (temp &gt; length) {
		    length = temp;
		}
	    }
	    return length;
	}
    }

}

