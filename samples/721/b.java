import com.google.common.base.Ascii;
import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Map.Entry;

class MediaType {
    /**
    * Returns a new instance with the same type and subtype as this instance, but without any
    * parameters.
    */
    public MediaType withoutParameters() {
	return parameters.isEmpty() ? this : create(type, subtype);
    }

    private final ImmutableListMultimap&lt;String, String&gt; parameters;
    private final String type;
    private final String subtype;
    @LazyInit
    private Optional&lt;Charset&gt; parsedCharset;
    private static final String WILDCARD = "*";
    private static final Map&lt;MediaType, MediaType&gt; KNOWN_TYPES = Maps.newHashMap();
    /** Matcher for type, subtype and attributes. */
    private static final CharMatcher TOKEN_MATCHER = ascii().and(javaIsoControl().negate()).and(CharMatcher.isNot(' '))
	    .and(CharMatcher.noneOf("()&lt;&gt;@,;:\\\"/[]?="));
    private static final String CHARSET_ATTRIBUTE = "charset";

    /**
    * Creates a new media type with the given type and subtype.
    *
    * @throws IllegalArgumentException if type or subtype is invalid or if a wildcard is used for the
    *     type, but not the subtype.
    */
    public static MediaType create(String type, String subtype) {
	MediaType mediaType = create(type, subtype, ImmutableListMultimap.&lt;String, String&gt;of());
	mediaType.parsedCharset = Optional.absent();
	return mediaType;
    }

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

}

