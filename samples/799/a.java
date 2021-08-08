import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class StrSubstitutor {
    /**
     * Replaces all the occurrences of variables in the given source object with their matching
     * values from the properties.
     *
     * @param source the source text containing the variables to substitute, null returns null
     * @param valueProperties the properties with values, may be null
     * @return the result of the replace operation
     */
    public static String replace(final Object source, final Properties valueProperties) {
	if (valueProperties == null) {
	    return source.toString();
	}
	final Map&lt;String, String&gt; valueMap = new HashMap&lt;&gt;();
	final Enumeration&lt;?&gt; propNames = valueProperties.propertyNames();
	while (propNames.hasMoreElements()) {
	    final String propName = (String) propNames.nextElement();
	    final String propValue = valueProperties.getProperty(propName);
	    valueMap.put(propName, propValue);
	}
	return replace(source, valueMap);
    }

    /**
     * Constant for the default variable prefix.
     */
    public static final StrMatcher DEFAULT_PREFIX = StrMatcher.stringMatcher("${");
    /**
     * Constant for the default variable suffix.
     */
    public static final StrMatcher DEFAULT_SUFFIX = StrMatcher.stringMatcher("}");
    /**
     * Constant for the default escape character.
     */
    public static final char DEFAULT_ESCAPE = '$';
    /**
     * Constant for the default value delimiter of a variable.
     * @since 3.2
     */
    public static final StrMatcher DEFAULT_VALUE_DELIMITER = StrMatcher.stringMatcher(":-");
    /**
     * Whether escapes should be preserved.  Default is false;
     */
    private boolean preserveEscapes = false;
    /**
     * Variable resolution is delegated to an implementor of VariableResolver.
     */
    private StrLookup&lt;?&gt; variableResolver;
    /**
     * Stores the variable prefix.
     */
    private StrMatcher prefixMatcher;
    /**
     * Stores the variable suffix.
     */
    private StrMatcher suffixMatcher;
    /**
     * Stores the escape character.
     */
    private char escapeChar;
    /**
     * Stores the default variable value delimiter
     */
    private StrMatcher valueDelimiterMatcher;
    /**
     * The flag whether substitution in variable names is enabled.
     */
    private boolean enableSubstitutionInVariables;

    /**
     * Replaces all the occurrences of variables in the given source object with
     * their matching values from the map.
     *
     * @param &lt;V&gt; the type of the values in the map
     * @param source  the source text containing the variables to substitute, null returns null
     * @param valueMap  the map with the values, may be null
     * @return the result of the replace operation
     */
    public static &lt;V&gt; String replace(final Object source, final Map&lt;String, V&gt; valueMap) {
	return new StrSubstitutor(valueMap).replace(source);
    }

    /**
     * Creates a new instance and initializes it. Uses defaults for variable
     * prefix and suffix and the escaping character.
     *
     * @param &lt;V&gt; the type of the values in the map
     * @param valueMap  the map with the variables' values, may be null
     */
    public &lt;V&gt; StrSubstitutor(final Map&lt;String, V&gt; valueMap) {
	this(StrLookup.mapLookup(valueMap), DEFAULT_PREFIX, DEFAULT_SUFFIX, DEFAULT_ESCAPE);
    }

    /**
     * Replaces all the occurrences of variables in the given source object with
     * their matching values from the resolver. The input source object is
     * converted to a string using &lt;code&gt;toString&lt;/code&gt; and is not altered.
     *
     * @param source  the source to replace in, null returns null
     * @return the result of the replace operation
     */
    public String replace(final Object source) {
	if (source == null) {
	    return null;
	}
	final StrBuilder buf = new StrBuilder().append(source);
	substitute(buf, 0, buf.length());
	return buf.toString();
    }

    /**
     * Creates a new instance and initializes it.
     *
     * @param variableResolver  the variable resolver, may be null
     * @param prefixMatcher  the prefix for variables, not null
     * @param suffixMatcher  the suffix for variables, not null
     * @param escape  the escape character
     * @throws IllegalArgumentException if the prefix or suffix is null
     */
    public StrSubstitutor(final StrLookup&lt;?&gt; variableResolver, final StrMatcher prefixMatcher,
	    final StrMatcher suffixMatcher, final char escape) {
	this(variableResolver, prefixMatcher, suffixMatcher, escape, DEFAULT_VALUE_DELIMITER);
    }

    /**
     * Internal method that substitutes the variables.
     * &lt;p&gt;
     * Most users of this class do not need to call this method. This method will
     * be called automatically by another (public) method.
     * &lt;p&gt;
     * Writers of subclasses can override this method if they need access to
     * the substitution process at the start or end.
     *
     * @param buf  the string builder to substitute into, not null
     * @param offset  the start offset within the builder, must be valid
     * @param length  the length within the builder to be processed, must be valid
     * @return true if altered
     */
    protected boolean substitute(final StrBuilder buf, final int offset, final int length) {
	return substitute(buf, offset, length, null) &gt; 0;
    }

    /**
     * Creates a new instance and initializes it.
     *
     * @param variableResolver  the variable resolver, may be null
     * @param prefixMatcher  the prefix for variables, not null
     * @param suffixMatcher  the suffix for variables, not null
     * @param escape  the escape character
     * @param valueDelimiterMatcher  the variable default value delimiter matcher, may be null
     * @throws IllegalArgumentException if the prefix or suffix is null
     * @since 3.2
     */
    public StrSubstitutor(final StrLookup&lt;?&gt; variableResolver, final StrMatcher prefixMatcher,
	    final StrMatcher suffixMatcher, final char escape, final StrMatcher valueDelimiterMatcher) {
	this.setVariableResolver(variableResolver);
	this.setVariablePrefixMatcher(prefixMatcher);
	this.setVariableSuffixMatcher(suffixMatcher);
	this.setEscapeChar(escape);
	this.setValueDelimiterMatcher(valueDelimiterMatcher);
    }

    /**
     * Recursive handler for multiple levels of interpolation. This is the main
     * interpolation method, which resolves the values of all variable references
     * contained in the passed in text.
     *
     * @param buf  the string builder to substitute into, not null
     * @param offset  the start offset within the builder, must be valid
     * @param length  the length within the builder to be processed, must be valid
     * @param priorVariables  the stack keeping track of the replaced variables, may be null
     * @return the length change that occurs, unless priorVariables is null when the int
     *  represents a boolean flag as to whether any change occurred.
     */
    private int substitute(final StrBuilder buf, final int offset, final int length, List&lt;String&gt; priorVariables) {
	final StrMatcher pfxMatcher = getVariablePrefixMatcher();
	final StrMatcher suffMatcher = getVariableSuffixMatcher();
	final char escape = getEscapeChar();
	final StrMatcher valueDelimMatcher = getValueDelimiterMatcher();
	final boolean substitutionInVariablesEnabled = isEnableSubstitutionInVariables();

	final boolean top = priorVariables == null;
	boolean altered = false;
	int lengthChange = 0;
	char[] chars = buf.buffer;
	int bufEnd = offset + length;
	int pos = offset;
	while (pos &lt; bufEnd) {
	    final int startMatchLen = pfxMatcher.isMatch(chars, pos, offset, bufEnd);
	    if (startMatchLen == 0) {
		pos++;
	    } else {
		// found variable start marker
		if (pos &gt; offset && chars[pos - 1] == escape) {
		    // escaped
		    if (preserveEscapes) {
			pos++;
			continue;
		    }
		    buf.deleteCharAt(pos - 1);
		    chars = buf.buffer; // in case buffer was altered
		    lengthChange--;
		    altered = true;
		    bufEnd--;
		} else {
		    // find suffix
		    final int startPos = pos;
		    pos += startMatchLen;
		    int endMatchLen = 0;
		    int nestedVarCount = 0;
		    while (pos &lt; bufEnd) {
			if (substitutionInVariablesEnabled
				&& (endMatchLen = pfxMatcher.isMatch(chars, pos, offset, bufEnd)) != 0) {
			    // found a nested variable start
			    nestedVarCount++;
			    pos += endMatchLen;
			    continue;
			}

			endMatchLen = suffMatcher.isMatch(chars, pos, offset, bufEnd);
			if (endMatchLen == 0) {
			    pos++;
			} else {
			    // found variable end marker
			    if (nestedVarCount == 0) {
				String varNameExpr = new String(chars, startPos + startMatchLen,
					pos - startPos - startMatchLen);
				if (substitutionInVariablesEnabled) {
				    final StrBuilder bufName = new StrBuilder(varNameExpr);
				    substitute(bufName, 0, bufName.length());
				    varNameExpr = bufName.toString();
				}
				pos += endMatchLen;
				final int endPos = pos;

				String varName = varNameExpr;
				String varDefaultValue = null;

				if (valueDelimMatcher != null) {
				    final char[] varNameExprChars = varNameExpr.toCharArray();
				    int valueDelimiterMatchLen = 0;
				    for (int i = 0; i &lt; varNameExprChars.length; i++) {
					// if there's any nested variable when nested variable substitution disabled, then stop resolving name and default value.
					if (!substitutionInVariablesEnabled && pfxMatcher.isMatch(varNameExprChars, i,
						i, varNameExprChars.length) != 0) {
					    break;
					}
					if ((valueDelimiterMatchLen = valueDelimMatcher.isMatch(varNameExprChars,
						i)) != 0) {
					    varName = varNameExpr.substring(0, i);
					    varDefaultValue = varNameExpr.substring(i + valueDelimiterMatchLen);
					    break;
					}
				    }
				}

				// on the first call initialize priorVariables
				if (priorVariables == null) {
				    priorVariables = new ArrayList&lt;&gt;();
				    priorVariables.add(new String(chars, offset, length));
				}

				// handle cyclic substitution
				checkCyclicSubstitution(varName, priorVariables);
				priorVariables.add(varName);

				// resolve the variable
				String varValue = resolveVariable(varName, buf, startPos, endPos);
				if (varValue == null) {
				    varValue = varDefaultValue;
				}
				if (varValue != null) {
				    // recursive replace
				    final int varLen = varValue.length();
				    buf.replace(startPos, endPos, varValue);
				    altered = true;
				    int change = substitute(buf, startPos, varLen, priorVariables);
				    change = change + varLen - (endPos - startPos);
				    pos += change;
				    bufEnd += change;
				    lengthChange += change;
				    chars = buf.buffer; // in case buffer was
							// altered
				}

				// remove variable from the cyclic stack
				priorVariables.remove(priorVariables.size() - 1);
				break;
			    }
			    nestedVarCount--;
			    pos += endMatchLen;
			}
		    }
		}
	    }
	}
	if (top) {
	    return altered ? 1 : 0;
	}
	return lengthChange;
    }

    /**
     * Sets the VariableResolver that is used to lookup variables.
     *
     * @param variableResolver  the VariableResolver
     */
    public void setVariableResolver(final StrLookup&lt;?&gt; variableResolver) {
	this.variableResolver = variableResolver;
    }

    /**
     * Sets the variable prefix matcher currently in use.
     * &lt;p&gt;
     * The variable prefix is the character or characters that identify the
     * start of a variable. This prefix is expressed in terms of a matcher
     * allowing advanced prefix matches.
     *
     * @param prefixMatcher  the prefix matcher to use, null ignored
     * @return this, to enable chaining
     * @throws IllegalArgumentException if the prefix matcher is null
     */
    public StrSubstitutor setVariablePrefixMatcher(final StrMatcher prefixMatcher) {
	if (prefixMatcher == null) {
	    throw new IllegalArgumentException("Variable prefix matcher must not be null!");
	}
	this.prefixMatcher = prefixMatcher;
	return this;
    }

    /**
     * Sets the variable suffix matcher currently in use.
     * &lt;p&gt;
     * The variable suffix is the character or characters that identify the
     * end of a variable. This suffix is expressed in terms of a matcher
     * allowing advanced suffix matches.
     *
     * @param suffixMatcher  the suffix matcher to use, null ignored
     * @return this, to enable chaining
     * @throws IllegalArgumentException if the suffix matcher is null
     */
    public StrSubstitutor setVariableSuffixMatcher(final StrMatcher suffixMatcher) {
	if (suffixMatcher == null) {
	    throw new IllegalArgumentException("Variable suffix matcher must not be null!");
	}
	this.suffixMatcher = suffixMatcher;
	return this;
    }

    /**
     * Sets the escape character.
     * If this character is placed before a variable reference in the source
     * text, this variable will be ignored.
     *
     * @param escapeCharacter  the escape character (0 for disabling escaping)
     */
    public void setEscapeChar(final char escapeCharacter) {
	this.escapeChar = escapeCharacter;
    }

    /**
     * Sets the variable default value delimiter matcher to use.
     * &lt;p&gt;
     * The variable default value delimiter is the character or characters that delimit the
     * variable name and the variable default value. This delimiter is expressed in terms of a matcher
     * allowing advanced variable default value delimiter matches.
     * &lt;p&gt;
     * If the &lt;code&gt;valueDelimiterMatcher&lt;/code&gt; is null, then the variable default value resolution
     * becomes disabled.
     *
     * @param valueDelimiterMatcher  variable default value delimiter matcher to use, may be null
     * @return this, to enable chaining
     * @since 3.2
     */
    public StrSubstitutor setValueDelimiterMatcher(final StrMatcher valueDelimiterMatcher) {
	this.valueDelimiterMatcher = valueDelimiterMatcher;
	return this;
    }

    /**
     * Gets the variable prefix matcher currently in use.
     * &lt;p&gt;
     * The variable prefix is the character or characters that identify the
     * start of a variable. This prefix is expressed in terms of a matcher
     * allowing advanced prefix matches.
     *
     * @return the prefix matcher in use
     */
    public StrMatcher getVariablePrefixMatcher() {
	return prefixMatcher;
    }

    /**
     * Gets the variable suffix matcher currently in use.
     * &lt;p&gt;
     * The variable suffix is the character or characters that identify the
     * end of a variable. This suffix is expressed in terms of a matcher
     * allowing advanced suffix matches.
     *
     * @return the suffix matcher in use
     */
    public StrMatcher getVariableSuffixMatcher() {
	return suffixMatcher;
    }

    /**
     * Returns the escape character.
     *
     * @return the character used for escaping variable references
     */
    public char getEscapeChar() {
	return this.escapeChar;
    }

    /**
     * Gets the variable default value delimiter matcher currently in use.
     * &lt;p&gt;
     * The variable default value delimiter is the character or characters that delimit the
     * variable name and the variable default value. This delimiter is expressed in terms of a matcher
     * allowing advanced variable default value delimiter matches.
     * &lt;p&gt;
     * If it returns null, then the variable default value resolution is disabled.
     *
     * @return the variable default value delimiter matcher in use, may be null
     * @since 3.2
     */
    public StrMatcher getValueDelimiterMatcher() {
	return valueDelimiterMatcher;
    }

    /**
     * Returns a flag whether substitution is done in variable names.
     *
     * @return the substitution in variable names flag
     * @since 3.0
     */
    public boolean isEnableSubstitutionInVariables() {
	return enableSubstitutionInVariables;
    }

    /**
     * Checks if the specified variable is already in the stack (list) of variables.
     *
     * @param varName  the variable name to check
     * @param priorVariables  the list of prior variables
     */
    private void checkCyclicSubstitution(final String varName, final List&lt;String&gt; priorVariables) {
	if (priorVariables.contains(varName) == false) {
	    return;
	}
	final StrBuilder buf = new StrBuilder(256);
	buf.append("Infinite loop in property interpolation of ");
	buf.append(priorVariables.remove(0));
	buf.append(": ");
	buf.appendWithSeparators(priorVariables, "-&gt;");
	throw new IllegalStateException(buf.toString());
    }

    /**
     * Internal method that resolves the value of a variable.
     * &lt;p&gt;
     * Most users of this class do not need to call this method. This method is
     * called automatically by the substitution process.
     * &lt;p&gt;
     * Writers of subclasses can override this method if they need to alter
     * how each substitution occurs. The method is passed the variable's name
     * and must return the corresponding value. This implementation uses the
     * {@link #getVariableResolver()} with the variable's name as the key.
     *
     * @param variableName  the name of the variable, not null
     * @param buf  the buffer where the substitution is occurring, not null
     * @param startPos  the start position of the variable including the prefix, valid
     * @param endPos  the end position of the variable including the suffix, valid
     * @return the variable's value or &lt;b&gt;null&lt;/b&gt; if the variable is unknown
     */
    protected String resolveVariable(final String variableName, final StrBuilder buf, final int startPos,
	    final int endPos) {
	final StrLookup&lt;?&gt; resolver = getVariableResolver();
	if (resolver == null) {
	    return null;
	}
	return resolver.lookup(variableName);
    }

    /**
     * Gets the VariableResolver that is used to lookup variables.
     *
     * @return the VariableResolver
     */
    public StrLookup&lt;?&gt; getVariableResolver() {
	return this.variableResolver;
    }

}

