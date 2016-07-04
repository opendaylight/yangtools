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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

public final class BindingMapping {

    public static final String VERSION = "0.6";

    public static final Set<String> JAVA_RESERVED_WORDS = ImmutableSet.of("abstract", "assert", "boolean", "break",
            "byte", "case", "catch", "char", "class", "const", "continue", "default", "double", "do", "else", "enum",
            "extends", "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
            "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while");

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
    public static final String MODEL_BINDING_PROVIDER_CLASS_NAME = "$YangModelBindingProvider";

    public static final String RPC_INPUT_SUFFIX = "Input";
    public static final String RPC_OUTPUT_SUFFIX = "Output";

    private static final ThreadLocal<SimpleDateFormat> PACKAGE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyMMdd");
        }

        @Override
        public void set(final SimpleDateFormat value) {
            throw new UnsupportedOperationException();
        }
    };

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
            }
        }

        packageNameBuilder.append(chars);
        packageNameBuilder.append(".rev");
        packageNameBuilder.append(PACKAGE_DATE_FORMAT.get().format(module.getRevision()));
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

    public static String getMethodName(final QName name) {
        checkArgument(name != null, "Name should not be null.");
        return getMethodName(name.getLocalName());
    }

    public static String getClassName(final String localName) {
        checkArgument(localName != null, "Name should not be null.");
        return toFirstUpper(toCamelCase(localName));
    }

    public static String getMethodName(final String yangIdentifier) {
        checkArgument(yangIdentifier != null,"Identifier should not be null");
        return toFirstLower(toCamelCase(yangIdentifier));
    }

    public static String getClassName(final QName name) {
        checkArgument(name != null, "Name should not be null.");
        return toFirstUpper(toCamelCase(name.getLocalName()));
    }

    public static String getGetterSuffix(final QName name) {
        checkArgument(name != null, "Name should not be null.");
        final String candidate = toFirstUpper(toCamelCase(name.getLocalName()));
        return ("Class".equals(candidate) ? "XmlClass" : candidate);
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
     * Returns the {@link String} {@code s} with an
     * {@link Character#isUpperCase(char) upper case} first character. This
     * function is null-safe.
     *
     * @param s
     *            the string that should get an upper case first character. May
     *            be <code>null</code>.
     * @return the {@link String} {@code s} with an upper case first character
     *         or <code>null</code> if the input {@link String} {@code s} was
     *         <code>null</code>.
     */
    public static String toFirstUpper(final String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        }
        if (s.length() == 1) {
            return s.toUpperCase();
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Returns the {@link String} {@code s} with an
     * {@link Character#isLowerCase(char) lower case} first character. This
     * function is null-safe.
     *
     * @param s
     *            the string that should get an lower case first character. May
     *            be <code>null</code>.
     * @return the {@link String} {@code s} with an lower case first character
     *         or <code>null</code> if the input {@link String} {@code s} was
     *         <code>null</code>.
     */
    private static String toFirstLower(final String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        if (s.length() == 1) {
            return s.toLowerCase();
        }
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }
}
