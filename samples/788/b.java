import java.util.Set;

class Zone {
    /**
     * Forces to add "MET" to the target zone table. This is because
     * there is a conflict between Java zone name "WET" and Olson zone
     * name.
     */
    static void addMET() {
	if (targetZones != null) {
	    targetZones.add("MET");
	}
    }

    private static Set&lt;String&gt; targetZones;

}

