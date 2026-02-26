/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import java.util.regex.Pattern;

/**
 * Contains the methods for converting strings to valid JAVA language strings
 * (package names, class names, attribute names) and to valid javadoc comments.
 */
@Beta
public final class BindingGeneratorUtil {
    /**
     * Pre-compiled replacement pattern.
     */
    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');
    private static final Pattern UNICODE_CHAR_PATTERN = Pattern.compile("\\\\+u");

    private BindingGeneratorUtil() {
        // Hidden on purpose
    }

    /**
     * Encodes angle brackets in yang statement description.
     *
     * @param description description of a yang statement which is used to generate javadoc comments
     * @return string with encoded angle brackets
     */
    public static String encodeAngleBrackets(String description) {
        if (description != null) {
            description = LT_MATCHER.replaceFrom(description, "&lt;");
            description = GT_MATCHER.replaceFrom(description, "&gt;");
        }
        return description;
    }

    /**
     * Escape potential unicode references so that the resulting string is safe to put into a {@code .java} file. This
     * processing is required to ensure this text we want to append does not end up with eligible backslashes. See
     * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.3">Java Language Specification</a>
     * for more information.
     *
     * @param str Input string
     * @return A string with all backslashes made ineligible
     */
    public static String replaceAllIllegalChars(final String str) {
        final int backslash = str.indexOf('\\');
        return backslash == -1 ? str : defangUnicodeEscapes(str);
    }

    private static String defangUnicodeEscapes(final String str) {
        // TODO: we should be able to receive the first offset from the non-deprecated method and perform a manual
        //       check for eligibility and escape -- that would be faster I think.
        final var ret = UNICODE_CHAR_PATTERN.matcher(str).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
    }
}
