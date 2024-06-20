/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.contract;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.regex.qual.Regex;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.BindingContract;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.ScalarTypeObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangDataName;

@Beta
public final class Naming {

    public static final @NonNull String VERSION = "0.6";

    // Note: these are not just JLS keywords, but rather character sequences which are reserved in codegen contexts
    public static final ImmutableSet<String> JAVA_RESERVED_WORDS = ImmutableSet.of(
        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.9 except module-info.java constructs
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
        "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "_",
        // "open", "module", "requires", "transitive", "exports, "opens", "to", "uses", "provides", "with",

        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.10.3
        "false", "true",
        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.10.7
        "null",
        // https://docs.oracle.com/javase/specs/jls/se10/html/jls-3.html#jls-3.9
        "var",
        // https://docs.oracle.com/javase/specs/jls/se14/html/jls-3.html#jls-3.9
        "yield",
        // https://docs.oracle.com/javase/specs/jls/se16/html/jls-3.html#jls-3.9
        "record");

    public static final @NonNull String DATA_ROOT_SUFFIX = "Data";
    public static final @NonNull String BUILDER_SUFFIX = "Builder";
    public static final @NonNull String KEY_SUFFIX = "Key";
    // ietf-restconf:yang-data, i.e. YangDataName
    public static final @NonNull String NAME_STATIC_FIELD_NAME = "NAME";
    // everything that can have a QName (e.g. identifier bound to a namespace)
    public static final @NonNull String QNAME_STATIC_FIELD_NAME = "QNAME";
    // concrete extensible contracts, for example 'feature', 'identity' and similar
    public static final @NonNull String VALUE_STATIC_FIELD_NAME = "VALUE";
    public static final @NonNull String PACKAGE_PREFIX = "org.opendaylight.yang.gen.v1";
    public static final @NonNull String SVC_PACKAGE_PREFIX = "org.opendaylight.yang.svc.v1";
    public static final @NonNull String AUGMENTATION_FIELD = "augmentation";

    private static final Splitter CAMEL_SPLITTER = Splitter.on(CharMatcher.anyOf(" _.-/").precomputed())
            .omitEmptyStrings().trimResults();
    private static final Pattern COLON_SLASH_SLASH = Pattern.compile("://", Pattern.LITERAL);
    private static final String QUOTED_DOT = Matcher.quoteReplacement(".");
    private static final Splitter DOT_SPLITTER = Splitter.on('.');

    public static final @NonNull String MODULE_INFO_CLASS_NAME = "YangModuleInfoImpl";
    public static final @NonNull String MODULE_INFO_QNAMEOF_METHOD_NAME = "qnameOf";
    public static final @NonNull String MODULE_INFO_YANGDATANAMEOF_METHOD_NAME = "yangDataNameOf";
    public static final @NonNull String MODEL_BINDING_PROVIDER_CLASS_NAME = "YangModelBindingProviderImpl";

    /**
     * Name of {@link Augmentable#augmentation(Class)}.
     */
    public static final @NonNull String AUGMENTABLE_AUGMENTATION_NAME = "augmentation";

    /**
     * Name of {@link KeyAware#key()}.
     */
    public static final @NonNull String KEY_AWARE_KEY_NAME = "key";

    /**
     * Name of {@link BindingContract#implementedInterface()}.
     */
    public static final @NonNull String BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME = "implementedInterface";

    /**
     * Name of default {@link Object#hashCode()} implementation for instantiated DataObjects. Each such generated
     * interface contains this static method.
     */
    public static final @NonNull String BINDING_HASHCODE_NAME = "bindingHashCode";

    /**
     * Name of default {@link Object#equals(Object)} implementation for instantiated DataObjects. Each such generated
     * interface contains this static method.
     */
    public static final @NonNull String BINDING_EQUALS_NAME = "bindingEquals";

    /**
     * Name of default {@link Object#toString()} implementation for instantiated DataObjects. Each such generated
     * interface contains this static method.
     */
    public static final @NonNull String BINDING_TO_STRING_NAME = "bindingToString";

    /**
     * Name of {@link Action#invoke(InstanceIdentifier, RpcInput)}.
     */
    public static final @NonNull String ACTION_INVOKE_NAME = "invoke";

    /**
     * Name of {@link Rpc#invoke(org.opendaylight.yangtools.yang.binding.RpcInput)}.
     */
    public static final @NonNull String RPC_INVOKE_NAME = "invoke";

    /**
     * Name of {@link ScalarTypeObject#getValue()}.
     */
    public static final @NonNull String SCALAR_TYPE_OBJECT_GET_VALUE_NAME = "getValue";

    /**
     * Prefix for normal getter methods.
     */
    public static final @NonNull String GETTER_PREFIX = "get";

    /**
     * Prefix for non-null default wrapper methods. These methods always wrap a corresponding normal getter.
     */
    public static final @NonNull String NONNULL_PREFIX = "nonnull";

    /**
     * Prefix for require default wrapper methods. These methods always wrap a corresponding normal getter
     * of leaf objects.
     */
    public static final @NonNull String REQUIRE_PREFIX = "require";
    public static final @NonNull String RPC_INPUT_SUFFIX = "Input";
    public static final @NonNull String RPC_OUTPUT_SUFFIX = "Output";

    private static final Interner<String> PACKAGE_INTERNER = Interners.newWeakInterner();
    @Regex
    private static final String ROOT_PACKAGE_PATTERN_STRING =
            "(org.opendaylight.yang.(gen|svc).v1.[a-z0-9_\\.]*?\\.(?:rev[0-9][0-9][0-1][0-9][0-3][0-9]|norev))";
    private static final Pattern ROOT_PACKAGE_PATTERN = Pattern.compile(ROOT_PACKAGE_PATTERN_STRING);

    private Naming() {
        // Hidden on purpose
    }

    /**
     * Return the package name for placing generated ServiceLoader entities, like {@link #MODULE_INFO_CLASS_NAME} and
     * {@link #MODEL_BINDING_PROVIDER_CLASS_NAME}.
     *
     * @param module module namespace
     * @return the package name for placing generated ServiceLoader entities
     */
    public static @NonNull String getServicePackageName(final QNameModule module) {
        final StringBuilder packageNameBuilder = new StringBuilder().append(SVC_PACKAGE_PREFIX).append('.');
        return getRootPackageName(packageNameBuilder, module);
    }

    public static @NonNull String getRootPackageName(final QName module) {
        return getRootPackageName(module.getModule());
    }

    public static @NonNull String getRootPackageName(final QNameModule module) {
        final StringBuilder packageNameBuilder = new StringBuilder().append(PACKAGE_PREFIX).append('.');
        return getRootPackageName(packageNameBuilder, module);
    }

    private static @NonNull String getRootPackageName(final StringBuilder builder, final QNameModule module) {
        String namespace = module.namespace().toString();
        namespace = COLON_SLASH_SLASH.matcher(namespace).replaceAll(QUOTED_DOT);

        final char[] chars = namespace.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            switch (chars[i]) {
                case '/', ':', '-', '@', '$', '#', '\'', '*', '+', ',', ';', '=' -> chars[i] = '.';
                default -> {
                    // no-op
                }
            }
        }

        builder.append(chars);
        if (chars[chars.length - 1] != '.') {
            builder.append('.');
        }

        final var revision = module.revision();
        if (revision != null) {
            // Revision is in format 2017-10-26, we want the output to be 171026, which is a matter of picking the
            // right characters.
            final String rev = revision.toString();
            verify(rev.length() == 10, "Revision.toString() resulted in unexpected '%s'", rev);
            builder.append("rev").append(rev, 2, 4).append(rev, 5, 7).append(rev.substring(8));
        } else {
            // No-revision packages are special
            builder.append("norev");
        }

        return normalizePackageName(builder.toString());
    }

    /**
     * Convert the result of {@link #getRootPackageName(QNameModule)} to the corresponding result of
     * {@link #getServicePackageName(QNameModule)}.
     *
     * @param rootPackageName root package name
     * @return Service root package name
     */
    public static @NonNull String rootToServicePackageName(final String rootPackageName) {
        final var match = ROOT_PACKAGE_PATTERN.matcher(rootPackageName);
        checkArgument(match.find(), "Package name '%s' does not match required pattern '%s'", rootPackageName,
            ROOT_PACKAGE_PATTERN_STRING);
        return getModelRootPackageName(rootPackageName.replace(Naming.PACKAGE_PREFIX, Naming.SVC_PACKAGE_PREFIX));
    }

    public static @NonNull String normalizePackageName(final String packageName) {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (String p : DOT_SPLITTER.split(packageName.toLowerCase(Locale.ENGLISH))) {
            if (first) {
                first = false;
            } else {
                builder.append('.');
            }

            if (Character.isDigit(p.charAt(0)) || JAVA_RESERVED_WORDS.contains(p)) {
                builder.append('_');
            }
            builder.append(p);
        }

        // Prevent duplication of input string
        return PACKAGE_INTERNER.intern(builder.toString());
    }

    public static @NonNull String getClassName(final String localName) {
        return toFirstUpper(toCamelCase(localName));
    }

    public static @NonNull String getClassName(final QName name) {
        return toFirstUpper(toCamelCase(name.getLocalName()));
    }

    public static @NonNull String getMethodName(final String yangIdentifier) {
        return toFirstLower(toCamelCase(yangIdentifier));
    }

    public static @NonNull String getMethodName(final QName name) {
        return getMethodName(name.getLocalName());
    }

    public static @NonNull String getGetterMethodName(final String localName) {
        return GETTER_PREFIX + toFirstUpper(getPropertyName(localName));
    }

    public static @NonNull String getGetterMethodName(final QName name) {
        return GETTER_PREFIX + getGetterSuffix(name);
    }

    public static boolean isGetterMethodName(final String methodName) {
        return methodName.startsWith(GETTER_PREFIX);
    }

    public static @NonNull String getGetterMethodForNonnull(final String methodName) {
        checkArgument(isNonnullMethodName(methodName));
        return GETTER_PREFIX + methodName.substring(NONNULL_PREFIX.length());
    }

    public static @NonNull String getNonnullMethodName(final String localName) {
        return NONNULL_PREFIX + toFirstUpper(getPropertyName(localName));
    }

    public static boolean isNonnullMethodName(final String methodName) {
        return methodName.startsWith(NONNULL_PREFIX);
    }

    public static @NonNull String getGetterMethodForRequire(final String methodName) {
        checkArgument(isRequireMethodName(methodName));
        return GETTER_PREFIX + methodName.substring(REQUIRE_PREFIX.length());
    }

    public static @NonNull String getRequireMethodName(final String localName) {
        return REQUIRE_PREFIX + toFirstUpper(getPropertyName(localName));
    }

    public static boolean isRequireMethodName(final String methodName) {
        return methodName.startsWith(REQUIRE_PREFIX);
    }

    public static @NonNull String getGetterSuffix(final QName name) {
        final String candidate = toFirstUpper(toCamelCase(name.getLocalName()));
        return "Class".equals(candidate) ? "XmlClass" : candidate;
    }

    public static @NonNull String getPropertyName(final String yangIdentifier) {
        final String potential = toFirstLower(toCamelCase(yangIdentifier));
        if ("class".equals(potential)) {
            return "xmlClass";
        }
        return potential;
    }

    // FIXME: this is legacy union/leafref property handling. The resulting value is *not* normalized for use as a
    //        property.
    public static @NonNull String getUnionLeafrefMemberName(final String unionClassSimpleName,
            final String referencedClassSimpleName) {
        return requireNonNull(referencedClassSimpleName) + requireNonNull(unionClassSimpleName) + "Value";
    }

    private static @NonNull String toCamelCase(final String rawString) {
        StringBuilder builder = new StringBuilder();
        for (String comp : CAMEL_SPLITTER.split(rawString)) {
            builder.append(toFirstUpper(comp));
        }
        return checkNumericPrefix(builder.toString());
    }

    private static @NonNull String checkNumericPrefix(final String rawString) {
        if (rawString.isEmpty()) {
            return rawString;
        }
        final char firstChar = rawString.charAt(0);
        return firstChar >= '0' && firstChar <= '9' ? "_" + rawString : rawString;
    }

    /**
     * Returns the {@link String} {@code s} with an {@link Character#isUpperCase(char) upper case} first character.
     *
     * @param str the string that should get an upper case first character.
     * @return the {@link String} {@code str} with an upper case first character.
     */
    public static @NonNull String toFirstUpper(final @NonNull String str) {
        if (str.isEmpty()) {
            return str;
        }
        if (Character.isUpperCase(str.charAt(0))) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase(Locale.ENGLISH);
        }
        return str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1);
    }

    /**
     * Returns the {@link String} {@code s} with a {@link Character#isLowerCase(char) lower case} first character. This
     * function is null-safe.
     *
     * @param str the string that should get an lower case first character. May be <code>null</code>.
     * @return the {@link String} {@code str} with an lower case first character or <code>null</code> if the input
     *         {@link String} {@code str} was empty.
     */
    private static @NonNull String toFirstLower(final @NonNull String str) {
        if (str.isEmpty()) {
            return str;
        }
        if (Character.isLowerCase(str.charAt(0))) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase(Locale.ENGLISH);
        }
        return str.substring(0, 1).toLowerCase(Locale.ENGLISH) + str.substring(1);
    }

    /**
     * Returns root package name for supplied package name.
     *
     * @param packageName Package for which find model root package.
     * @return Package of model root.
     * @throws NullPointerException if {@code packageName} is {@code null}
     * @throws IllegalArgumentException if {@code packageName} does not start with {@link #PACKAGE_PREFIX} or it does
     *                                  not match package name formatting rules
     */
    public static @NonNull String getModelRootPackageName(final String packageName) {
        checkArgument(packageName.startsWith(PACKAGE_PREFIX) || packageName.startsWith(SVC_PACKAGE_PREFIX),
            "Package name not starting with %s, is: %s", PACKAGE_PREFIX, packageName);
        final var match = ROOT_PACKAGE_PATTERN.matcher(packageName);
        checkArgument(match.find(), "Package name '%s' does not match required pattern '%s'", packageName,
            ROOT_PACKAGE_PATTERN_STRING);
        return match.group(0);
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

    /**
     * Builds class name representing yang-data template name which is not yang identifier compliant.
     *
     * @param templateName template name
     * @return Java class name
     * @throws NullPointerException if {@code templateName} is {@code null}
     * @throws IllegalArgumentException if (@code templateName} is empty
     */
    // TODO: take YangDataName once we have it readily available
    public static String mapYangDataName(final YangDataName templateName) {
        return mapEnumAssignedName(templateName.name());
    }

    // See https://docs.oracle.com/javase/specs/jls/se16/html/jls-3.html#jls-3.8
    // TODO: we are being conservative here, but should differentiate TypeIdentifier and UnqualifiedMethodIdentifier,
    //       which have different exclusions
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
