class RevertActor extends XmlActorAdapter {
    /**
     * @param filePrefix
     *            is used to generate the name of the reverted map in case that
     *            fileName is null.
     */
    public RevertXmlAction createRevertXmlAction(String xmlPackedFile, String fileName, String filePrefix) {
	RevertXmlAction revertXmlAction = new RevertXmlAction();
	revertXmlAction.setLocalFileName(fileName);
	revertXmlAction.setMap(xmlPackedFile);
	revertXmlAction.setFilePrefix(filePrefix);
	return revertXmlAction;
    }

}

