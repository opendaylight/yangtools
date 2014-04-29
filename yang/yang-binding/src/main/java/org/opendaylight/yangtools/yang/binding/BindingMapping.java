/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.*;

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

    private static final Splitter SPACE_SPLITTER = Splitter.on(" ").omitEmptyStrings().trimResults();

    public static final String MODULE_INFO_CLASS_NAME = "$YangModuleInfoImpl";
    public static final String MODEL_BINDING_PROVIDER_CLASS_NAME = "$YangModelBindingProvider";

    public static final String getMethodName(QName name) {
        checkArgument(name != null, "Name should not be null.");
        return getMethodName(name.getLocalName());
    }

    public static final String getClassName(String localName) {
        checkArgument(localName != null, "Name should not be null.");
        return toFirstUpper(toCamelCase(localName));
    }

    public static final String getMethodName(String yangIdentifier) {
        checkArgument(yangIdentifier != null,"Identifier should not be null");
        return toFirstLower(toCamelCase(yangIdentifier));
    }

    public static final String getClassName(QName name) {
        checkArgument(name != null, "Name should not be null.");
        return toFirstUpper(toCamelCase(name.getLocalName()));
    }

    public static String getPropertyName(String yangIdentifier) {
        final String potential = toFirstLower(toCamelCase(yangIdentifier));
        if("class".equals(potential)) {
            return "xmlClass";
        }
        return potential;
    }

    private static final String toCamelCase(String rawString) {
        checkArgument(rawString != null, "String should not be null");
        Iterable<String> components = SPACE_SPLITTER.split(rawString.replace('-', ' ').replace('_', ' '));
        StringBuilder builder = new StringBuilder();
        for (String comp : components) {
            builder.append(toFirstUpper(comp));
        }
        return builder.toString();
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
    private static String toFirstUpper(String s) {
        if (s == null || s.length() == 0)
            return s;
        if (Character.isUpperCase(s.charAt(0)))
            return s;
        if (s.length() == 1)
            return s.toUpperCase();
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
    private static String toFirstLower(String s) {
        if (s == null || s.length() == 0)
            return s;
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        if (s.length() == 1)
            return s.toLowerCase();
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }
}
