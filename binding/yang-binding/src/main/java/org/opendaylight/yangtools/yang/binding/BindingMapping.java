/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

public final class BindingMapping {

    public static final String VERSION = "0.6";

    public static final Set<String> JAVA_RESERVED_WORDS = ImmutableSet.of(
        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.9
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
        "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "_",
        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.10.3
        "false", "true",
        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.10.7
        "null");

    public static final String DATA_ROOT_SUFFIX = "Data";
    public static final String RPC_SERVICE_SUFFIX = "Service";
    public static final String NOTIFICATION_LISTENER_SUFFIX = "Listener";
    public static final String QNAME_STATIC_FIELD_NAME = "QNAME";
    public static final String PACKAGE_PREFIX = "org.opendaylight.yang.gen.v1";
    public static final String AUGMENTATION_FIELD = "augmentation";

    private static final Splitter CAMEL_SPLITTER = Splitter.on(CharMatcher.anyOf(" _.-/").precomputed())
            .omitEmptyStrings().trimResults();
    private static final Pattern COLON_SLASH_SLASH = Pattern.compile("://", Pattern.LITERAL);
    private static final String QUOTED_DOT = Matcher.quoteReplacement(".");
    private static final Splitter DOT_SPLITTER = Splitter.on('.');

    public static final String MODULE_INFO_CLASS_NAME = "$YangModuleInfoImpl";
    public static final String MODULE_INFO_QNAMEOF_METHOD_NAME = "qnameOf";
    public static final String MODEL_BINDING_PROVIDER_CLASS_NAME = "$YangModelBindingProvider";

    /**
     * Name of {@link Augmentable#augmentation(Class)}.
     */
    public static final String AUGMENTABLE_AUGMENTATION_NAME = "augmentation";

    /**
     * Name of {@link Identifiable#key()}.
     */
    public static final String IDENTIFIABLE_KEY_NAME = "key";

    public static final String RPC_INPUT_SUFFIX = "Input";
    public static final String RPC_OUTPUT_SUFFIX = "Output";

    private static final String NEGATED_PATTERN_PREFIX = "^(?!";
    private static final String NEGATED_PATTERN_SUFFIX = ").*$";

    private static final Interner<String> PACKAGE_INTERNER = Interners.newWeakInterner();

    private BindingMapping() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static String getRootPackageName(final QName module) {
        return getRootPackageName(module.getModule());
    }

    public static String getRootPackageName(final QNameModule module) {
        checkArgument(module != null, "Module must not be null");
        checkArgument(module.getRevision() != null, "Revision must not be null");
        checkArgument(module.getNamespace() != null, "Namespace must not be null");
        final StringBuilder packageNameBuilder = new StringBuilder();

        packageNameBuilder.append(BindingMapping.PACKAGE_PREFIX);
        packageNameBuilder.append('.');

        String namespace = module.getNamespace().toString();
        namespace = COLON_SLASH_SLASH.matcher(namespace).replaceAll(QUOTED_DOT);

        final char[] chars = namespace.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            switch (chars[i]) {
                case '/':
                case ':':
                case '-':
                case '@':
                case '$':
                case '#':
                case '\'':
                case '*':
                case '+':
                case ',':
                case ';':
                case '=':
                    chars[i] = '.';
                    break;
                default:
                    // no-op
            }
        }

        packageNameBuilder.append(chars);
        if (chars[chars.length - 1] != '.') {
            packageNameBuilder.append('.');
        }

        final Optional<Revision> optRev = module.getRevision();
        if (optRev.isPresent()) {
            // Revision is in format 2017-10-26, we want the output to be 171026, which is a matter of picking the
            // right characters.
            final String rev = optRev.get().toString();
            checkArgument(rev.length() == 10, "Unsupported revision %s", rev);
            packageNameBuilder.append("rev");
            packageNameBuilder.append(rev.substring(2, 4)).append(rev.substring(5, 7)).append(rev.substring(8));
        } else {
            // No-revision packages are special
            packageNameBuilder.append("norev");
        }

        return normalizePackageName(packageNameBuilder.toString());
    }

    public static String normalizePackageName(final String packageName) {
        if (packageName == null) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (String p : DOT_SPLITTER.split(packageName.toLowerCase())) {
            if (first) {
                first = false;
            } else {
                builder.append('.');
            }

            if (Character.isDigit(p.charAt(0)) || BindingMapping.JAVA_RESERVED_WORDS.contains(p)) {
                builder.append('_');
            }
            builder.append(p);
        }

        // Prevent duplication of input string
        return PACKAGE_INTERNER.intern(builder.toString());
    }

    public static String getClassName(final String localName) {
        checkArgument(localName != null, "Name should not be null.");
        return toFirstUpper(toCamelCase(localName));
    }

    public static String getClassName(final QName name) {
        checkArgument(name != null, "Name should not be null.");
        return toFirstUpper(toCamelCase(name.getLocalName()));
    }

    public static String getMethodName(final String yangIdentifier) {
        checkArgument(yangIdentifier != null,"Identifier should not be null");
        return toFirstLower(toCamelCase(yangIdentifier));
    }

    public static String getMethodName(final QName name) {
        checkArgument(name != null, "Name should not be null.");
        return getMethodName(name.getLocalName());
    }

    public static String getGetterSuffix(final QName name) {
        checkArgument(name != null, "Name should not be null.");
        final String candidate = toFirstUpper(toCamelCase(name.getLocalName()));
        return "Class".equals(candidate) ? "XmlClass" : candidate;
    }

    public static String getPropertyName(final String yangIdentifier) {
        final String potential = toFirstLower(toCamelCase(yangIdentifier));
        if ("class".equals(potential)) {
            return "xmlClass";
        }
        return potential;
    }

    private static String toCamelCase(final String rawString) {
        checkArgument(rawString != null, "String should not be null");
        Iterable<String> components = CAMEL_SPLITTER.split(rawString);
        StringBuilder builder = new StringBuilder();
        for (String comp : components) {
            builder.append(toFirstUpper(comp));
        }
        return checkNumericPrefix(builder.toString());
    }

    private static String checkNumericPrefix(final String rawString) {
        if (rawString == null || rawString.isEmpty()) {
            return rawString;
        }
        char firstChar = rawString.charAt(0);
        if (firstChar >= '0' && firstChar <= '9') {
            return "_" + rawString;
        } else {
            return rawString;
        }
    }

    /**
     * Create a {@link Pattern} expression which performs inverted match to the specified pattern. The input pattern
     * is expected to be a valid regular expression passing {@link Pattern#compile(String)} and to have both start and
     * end of string anchors as the first and last characters.
     *
     * @param pattern Pattern regular expression to negate
     * @return Negated regular expression
     * @throws IllegalArgumentException if the pattern does not conform to expected structure
     * @throws NullPointerException if pattern is null
     */
    public static String negatePatternString(final String pattern) {
        checkArgument(pattern.charAt(0) == '^' && pattern.charAt(pattern.length() - 1) == '$',
                "Pattern '%s' does not have expected format", pattern);

        /*
         * Converting the expression into a negation is tricky. For example, when we have:
         *
         *   pattern "a|b" { modifier invert-match; }
         *
         * this gets escaped into either "^a|b$" or "^(?:a|b)$". Either format can occur, as the non-capturing group
         * strictly needed only in some cases. From that we want to arrive at:
         *   "^(?!(?:a|b)$).*$".
         *
         *           ^^^         original expression
         *        ^^^^^^^^       tail of a grouped expression (without head anchor)
         *    ^^^^        ^^^^   inversion of match
         *
         * Inversion works by explicitly anchoring at the start of the string and then:
         * - specifying a negative lookahead until the end of string
         * - matching any string
         * - anchoring at the end of the string
         */
        final boolean hasGroup = pattern.startsWith("^(?:") && pattern.endsWith(")$");
        final int len = pattern.length();
        final StringBuilder sb = new StringBuilder(len + (hasGroup ? 7 : 11)).append(NEGATED_PATTERN_PREFIX);

        if (hasGroup) {
            sb.append(pattern, 1, len);
        } else {
            sb.append("(?:").append(pattern, 1, len - 1).append(")$");
        }
        return sb.append(NEGATED_PATTERN_SUFFIX).toString();
    }

    /**
     * Check if the specified {@link Pattern} is the result of {@link #negatePatternString(String)}. This method
     * assumes the pattern was not hand-coded but rather was automatically-generated, such that its non-automated
     * parts come from XSD regular expressions. If this constraint is violated, this method may result false positives.
     *
     * @param pattern Pattern to check
     * @return True if this pattern is a negation.
     * @throws NullPointerException if pattern is null
     * @throws IllegalArgumentException if the pattern does not conform to expected structure
     */
    public static boolean isNegatedPattern(final Pattern pattern) {
        return isNegatedPattern(pattern.toString());
    }

    private static boolean isNegatedPattern(final String pattern) {
        return pattern.startsWith(NEGATED_PATTERN_PREFIX) && pattern.endsWith(NEGATED_PATTERN_SUFFIX);
    }

    /**
     * Returns the {@link String} {@code s} with an {@link Character#isUpperCase(char) upper case} first character. This
     * function is null-safe.
     *
     * @param str the string that should get an upper case first character. May be <code>null</code>.
     * @return the {@link String} {@code str} with an upper case first character or <code>null</code> if the input
     *         {@link String} {@code str} was <code>null</code>.
     */
    public static String toFirstUpper(final String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (Character.isUpperCase(str.charAt(0))) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Returns the {@link String} {@code s} with a {@link Character#isLowerCase(char) lower case} first character. This
     * function is null-safe.
     *
     * @param str the string that should get an lower case first character. May be <code>null</code>.
     * @return the {@link String} {@code str} with an lower case first character or <code>null</code> if the input
     *         {@link String} {@code str} was <code>null</code>.
     */
    private static String toFirstLower(final String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (Character.isLowerCase(str.charAt(0))) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Returns Java identifiers, conforming to JLS9 Section 3.8 to use for specified YANG assigned names
     * (RFC7950 Section 9.6.4). This method considers two distinct encodings: one the pre-Fluorine mapping, which is
     * okay and convenient for sane strings, and an escaping-based bijective mapping which works for all possible
     * Unicode strings.
     *
     * @param assignedNames Collection of assigned names
     * @return A BiMap keyed by assigned name, with Java identifiers as values
     * @throws NullPointerException if assignedNames is null or contains null items
     * @throws IllegalArgumentException if any of the names is empty
     */
    public static BiMap<String, String> mapEnumAssignedNames(final Collection<String> assignedNames) {
        /*
         * Original mapping assumed strings encountered are identifiers, hence it used getClassName to map the names
         * and that function is not an injection -- this is evidenced in MDSAL-208 and results in a failure to compile
         * generated code. If we encounter such a conflict or if the result is not a valid identifier (like '*'), we
         * abort and switch the mapping schema to mapEnumAssignedName(), which is a bijection.
         *
         * Note that assignedNames can contain duplicates, which must not trigger a duplication fallback.
         */
        final BiMap<String, String> javaToYang = HashBiMap.create(assignedNames.size());
        boolean valid = true;
        for (String name : assignedNames) {
            checkArgument(!name.isEmpty());
            if (!javaToYang.containsValue(name)) {
                final String mappedName = getClassName(name);
                if (!isValidJavaIdentifier(mappedName) || javaToYang.forcePut(mappedName, name) != null) {
                    valid = false;
                    break;
                }
            }
        }

        if (!valid) {
            // Fall back to bijective mapping
            javaToYang.clear();
            for (String name : assignedNames) {
                javaToYang.put(mapEnumAssignedName(name), name);
            }
        }

        return javaToYang.inverse();
    }

    // See https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.8
    private static boolean isValidJavaIdentifier(final String str) {
        return !str.isEmpty() && !JAVA_RESERVED_WORDS.contains(str)
                && Character.isJavaIdentifierStart(str.codePointAt(0))
                && str.codePoints().skip(1).allMatch(Character::isJavaIdentifierPart);
    }

    private static String mapEnumAssignedName(final String assignedName) {
        checkArgument(!assignedName.isEmpty());

        // Mapping rules:
        // - if the string is a valid java identifier and does not contain '$', use it as-is
        if (assignedName.indexOf('$') == -1 && isValidJavaIdentifier(assignedName)) {
            return assignedName;
        }

        // - otherwise prefix it with '$' and replace any invalid character (including '$') with '$XX$', where XX is
        //   hex-encoded unicode codepoint (including plane, stripping leading zeroes)
        final StringBuilder sb = new StringBuilder().append('$');
        assignedName.codePoints().forEachOrdered(codePoint -> {
            if (codePoint == '$' || !Character.isJavaIdentifierPart(codePoint)) {
                sb.append('$').append(Integer.toHexString(codePoint).toUpperCase(Locale.ROOT)).append('$');
            } else {
                sb.appendCodePoint(codePoint);
            }
        });
        return sb.toString();
    }
}
