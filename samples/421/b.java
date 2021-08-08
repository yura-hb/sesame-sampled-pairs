import static com.google.common.base.CharMatcher.ascii;
import com.google.common.base.Ascii;
import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Map.Entry;

class MediaType {
    /**
    * Parses a media type from its string representation.
    *
    * @throws IllegalArgumentException if the input is not parsable
    */
    public static MediaType parse(String input) {
	checkNotNull(input);
	Tokenizer tokenizer = new Tokenizer(input);
	try {
	    String type = tokenizer.consumeToken(TOKEN_MATCHER);
	    tokenizer.consumeCharacter('/');
	    String subtype = tokenizer.consumeToken(TOKEN_MATCHER);
	    ImmutableListMultimap.Builder&lt;String, String&gt; parameters = ImmutableListMultimap.builder();
	    while (tokenizer.hasMore()) {
		tokenizer.consumeTokenIfPresent(LINEAR_WHITE_SPACE);
		tokenizer.consumeCharacter(';');
		tokenizer.consumeTokenIfPresent(LINEAR_WHITE_SPACE);
		String attribute = tokenizer.consumeToken(TOKEN_MATCHER);
		tokenizer.consumeCharacter('=');
		final String value;
		if ('"' == tokenizer.previewChar()) {
		    tokenizer.consumeCharacter('"');
		    StringBuilder valueBuilder = new StringBuilder();
		    while ('"' != tokenizer.previewChar()) {
			if ('\\' == tokenizer.previewChar()) {
			    tokenizer.consumeCharacter('\\');
			    valueBuilder.append(tokenizer.consumeCharacter(ascii()));
			} else {
			    valueBuilder.append(tokenizer.consumeToken(QUOTED_TEXT_MATCHER));
			}
		    }
		    value = valueBuilder.toString();
		    tokenizer.consumeCharacter('"');
		} else {
		    value = tokenizer.consumeToken(TOKEN_MATCHER);
		}
		parameters.put(attribute, value);
	    }
	    return create(type, subtype, parameters.build());
	} catch (IllegalStateException e) {
	    throw new IllegalArgumentException("Could not parse '" + input + "'", e);
	}
    }

    /** Matcher for type, subtype and attributes. */
    private static final CharMatcher TOKEN_MATCHER = ascii().and(javaIsoControl().negate()).and(CharMatcher.isNot(' '))
	    .and(CharMatcher.noneOf("()&lt;&gt;@,;:\\\"/[]?="));
    private static final CharMatcher LINEAR_WHITE_SPACE = CharMatcher.anyOf(" \t\r\n");
    private static final CharMatcher QUOTED_TEXT_MATCHER = ascii().and(CharMatcher.noneOf("\"\\\r"));
    private static final String WILDCARD = "*";
    private static final Map&lt;MediaType, MediaType&gt; KNOWN_TYPES = Maps.newHashMap();
    private static final String CHARSET_ATTRIBUTE = "charset";
    private final String type;
    private final String subtype;
    private final ImmutableListMultimap&lt;String, String&gt; parameters;

    private static MediaType create(String type, String subtype, Multimap&lt;String, String&gt; parameters) {
	checkNotNull(type);
	checkNotNull(subtype);
	checkNotNull(parameters);
	String normalizedType = normalizeToken(type);
	String normalizedSubtype = normalizeToken(subtype);
	checkArgument(!WILDCARD.equals(normalizedType) || WILDCARD.equals(normalizedSubtype),
		"A wildcard type cannot be used with a non-wildcard subtype");
	ImmutableListMultimap.Builder&lt;String, String&gt; builder = ImmutableListMultimap.builder();
	for (Entry&lt;String, String&gt; entry : parameters.entries()) {
	    String attribute = normalizeToken(entry.getKey());
	    builder.put(attribute, normalizeParameterValue(attribute, entry.getValue()));
	}
	MediaType mediaType = new MediaType(normalizedType, normalizedSubtype, builder.build());
	// Return one of the constants if the media type is a known type.
	return MoreObjects.firstNonNull(KNOWN_TYPES.get(mediaType), mediaType);
    }

    private static String normalizeToken(String token) {
	checkArgument(TOKEN_MATCHER.matchesAllOf(token));
	return Ascii.toLowerCase(token);
    }

    private static String normalizeParameterValue(String attribute, String value) {
	return CHARSET_ATTRIBUTE.equals(attribute) ? Ascii.toLowerCase(value) : value;
    }

    private MediaType(String type, String subtype, ImmutableListMultimap&lt;String, String&gt; parameters) {
	this.type = type;
	this.subtype = subtype;
	this.parameters = parameters;
    }

    class Tokenizer {
	/** Matcher for type, subtype and attributes. */
	private static final CharMatcher TOKEN_MATCHER = ascii().and(javaIsoControl().negate())
		.and(CharMatcher.isNot(' ')).and(CharMatcher.noneOf("()&lt;&gt;@,;:\\\"/[]?="));
	private static final CharMatcher LINEAR_WHITE_SPACE = CharMatcher.anyOf(" \t\r\n");
	private static final CharMatcher QUOTED_TEXT_MATCHER = ascii().and(CharMatcher.noneOf("\"\\\r"));
	private static final String WILDCARD = "*";
	private static final Map&lt;MediaType, MediaType&gt; KNOWN_TYPES = Maps.newHashMap();
	private static final String CHARSET_ATTRIBUTE = "charset";
	private final String type;
	private final String subtype;
	private final ImmutableListMultimap&lt;String, String&gt; parameters;

	Tokenizer(String input) {
	    this.input = input;
	}

	String consumeToken(CharMatcher matcher) {
	    int startPosition = position;
	    String token = consumeTokenIfPresent(matcher);
	    checkState(position != startPosition);
	    return token;
	}

	char consumeCharacter(char c) {
	    checkState(hasMore());
	    checkState(previewChar() == c);
	    position++;
	    return c;
	}

	boolean hasMore() {
	    return (position &gt;= 0) && (position &lt; input.length());
	}

	String consumeTokenIfPresent(CharMatcher matcher) {
	    checkState(hasMore());
	    int startPosition = position;
	    position = matcher.negate().indexIn(input, startPosition);
	    return hasMore() ? input.substring(startPosition, position) : input.substring(startPosition);
	}

	char previewChar() {
	    checkState(hasMore());
	    return input.charAt(position);
	}

	char consumeCharacter(CharMatcher matcher) {
	    checkState(hasMore());
	    char c = previewChar();
	    checkState(matcher.matches(c));
	    position++;
	    return c;
	}

    }

}

