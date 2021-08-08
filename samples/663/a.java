class NodeViewFactory {
    /**
     * Factory method which creates the right NodeView for the model.
     */
    NodeView newNodeView(MindMapNode model, int position, MapView map, Container parent) {
	NodeView newView = new NodeView(model, position, map, parent);

	if (model.isRoot()) {
	    final MainView mainView = new RootMainView();
	    newView.setMainView(mainView);
	    newView.setLayout(VerticalRootNodeViewLayout.getInstance());

	} else {
	    newView.setMainView(newMainView(model));
	    if (newView.isLeft()) {
		newView.setLayout(LeftNodeViewLayout.getInstance());
	    } else {
		newView.setLayout(RightNodeViewLayout.getInstance());
	    }
	}

	map.addViewer(model, newView);
	newView.update();
	fireNodeViewCreated(newView);
	return newView;
    }

    MainView newMainView(MindMapNode model) {
	if (model.isRoot()) {
	    return new RootMainView();
	}
	if (model.getStyle().equals(MindMapNode.STYLE_FORK)) {
	    return new ForkMainView();
	} else if (model.getStyle().equals(MindMapNode.STYLE_BUBBLE)) {
	    return new BubbleMainView();
	} else {
	    System.err.println("Tried to create a NodeView of unknown Style.");
	    return new ForkMainView();
	}
    }

    private void fireNodeViewCreated(NodeView newView) {
	newView.getMap().getViewFeedback().onViewCreatedHook(newView);
    }

}

