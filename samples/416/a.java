abstract class ControllerAdapter extends MapFeedbackAdapter implements ModeController, DirectoryResultListener {
    /**
     * Overwrite this method to perform additional operations to an node update.
     */
    protected void updateNode(MindMapNode node) {
	for (NodeSelectionListener listener : mNodeSelectionListeners) {
	    listener.onUpdateNodeHook(node);
	}
    }

    private HashSet&lt;NodeSelectionListener&gt; mNodeSelectionListeners = new HashSet&lt;&gt;();

}

