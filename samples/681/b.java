import javax.xml.parsers.SAXParserFactory;

class SAXParserTest02 {
    /**
     * Test to test the functionality of setValidating and isValidating
     * methods.
     *
     * @throws Exception If any errors occur.
     */
    @Test
    public void testValidate02() throws Exception {
	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setValidating(true);
	spf.newSAXParser();
	assertTrue(spf.isValidating());
    }

}

