abstract class DummyEvent implements XMLEvent {
    /** A utility function to check if this event is Characters.
     * @see Characters
     */
    public boolean isCharacters() {
	return fEventType == XMLEvent.CHARACTERS;
    }

    private int fEventType;

}

