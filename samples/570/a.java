class HtmlTools {
    /** \0 is not allowed: */
    public static String makeValidXml(String pXmlNoteText) {
	return pXmlNoteText.replaceAll("\0", "").replaceAll("&#0;", "");
    }

}

