class InputEqualsAvoidNull {
    /**
     * methods that should not get flagged
     */
    public void noFlagForEqualsIgnoreCase() {
	String s = "peperoni";
	String s1 = "tasty";

	s.equalsIgnoreCase(s += "mushrooms");

	s1.equalsIgnoreCase(s += "mushrooms");

	(s = "thin crust").equalsIgnoreCase("thick crust");

	(s += "garlic").equalsIgnoreCase("basil");

	("Chicago Style" + "NY Style").equalsIgnoreCase("California Style" + "Any Style");

	"onions".equalsIgnoreCase(s);

	s.equalsIgnoreCase(new String());

	s.equals(s1);

	new String().equalsIgnoreCase("more cheese");

    }

}

