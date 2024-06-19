/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.util.regex.Pattern;

@Beta
public final class RegexPatterns {
    private static final String NEGATED_PATTERN_PREFIX = "^(?!";
    private static final String NEGATED_PATTERN_SUFFIX = ").*$";

    private RegexPatterns() {

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
        final String str = pattern.toString();
        return str.startsWith(RegexPatterns.NEGATED_PATTERN_PREFIX)
                && str.endsWith(RegexPatterns.NEGATED_PATTERN_SUFFIX);
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
        final StringBuilder sb = new StringBuilder(len + (hasGroup ? 7 : 11))
                .append(NEGATED_PATTERN_PREFIX);

        if (hasGroup) {
            sb.append(pattern, 1, len);
        } else {
            sb.append("(?:").append(pattern, 1, len - 1).append(")$");
        }
        return sb.append(NEGATED_PATTERN_SUFFIX).toString();
    }
}
