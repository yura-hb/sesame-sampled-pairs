class Sources {
    /**
     * @bug 8144967
     * Tests that the source is not empty
     * @param source the Source object
     */
    @Test(dataProvider = "nonEmptySources")
    public void testIsNotEmpty(Source source) {
	Assert.assertTrue(!source.isEmpty(), "The source is empty");
    }

}

