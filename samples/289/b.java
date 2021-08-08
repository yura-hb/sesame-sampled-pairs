import java.util.Map;

class Zoneinfo {
    /**
     * Adds the given rule to the list of Rules.
     * @param rule Rule to be added to the list.
     */
    void add(Rule rule) {
	String name = rule.getName();
	rules.put(name, rule);
    }

    /**
     * Rule name to Rule mappings
     */
    private Map&lt;String, Rule&gt; rules;

}

