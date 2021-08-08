class MapSearchMarkerLocation extends MapMarkerBase {
    /**
     * Either start or when something changes on the node, this method is
     * called.
     */
    public void update() {
	setText(mPlace.getDisplayName());
	setForeground(mBulletColor);
	setSize(getPreferredSize());
    }

    private final Place mPlace;

}

