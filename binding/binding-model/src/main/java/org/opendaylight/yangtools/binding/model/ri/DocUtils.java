/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.TypeComment;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;

/**
 * A dumping ground for utilities shared between {@code binding-generator} and {@code binding-codegen} imposed by
 * current design of {@link GeneratedType}.
 *
 * @since 16.0.0
 */
@Beta
public final class DocUtils {
    // for encodeAngleBrackets()
    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');

    // for replaceAllIllegalChars()
    private static final Pattern UNICODE_CHAR_PATTERN = Pattern.compile("\\\\+u");

    // for typeCommentOf()
    private static final Escaper ENTITY_ESCAPER = Escapers.builder()
        .addEscape('<', "&lt;")
        .addEscape('>', "&gt;")
        .addEscape('&', "&amp;")
        .addEscape('@', "&#64;")
        .build();
    private static final Pattern TAIL_COMMENT_PATTERN = Pattern.compile("*/", Pattern.LITERAL);
    private static final CharMatcher NEWLINE_OR_TAB = CharMatcher.anyOf("\n\t");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile(" +");

    private DocUtils() {
        // hidden on purpose
    }

    /**
     * Encodes angle brackets in yang statement description.
     *
     * @param description description of a yang statement which is used to generate javadoc comments
     * @return string with encoded angle brackets
     */
    public static String encodeAngleBrackets(final String description) {
        return description == null ? null
            : GT_MATCHER.replaceFrom(LT_MATCHER.replaceFrom(description, "&lt;"), "&gt;");
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
    @NonNullByDefault
    public static String replaceAllIllegalChars(final String str) {
        final int backslash = str.indexOf('\\');
        return backslash == -1 ? str : defangUnicodeEscapes(str);
    }

    @NonNullByDefault
    private static String defangUnicodeEscapes(final String str) {
        // TODO: we should be able to receive the first offset from the non-deprecated method and perform a manual
        //       check for eligibility and escape -- that would be faster I think.
        final var ret = UNICODE_CHAR_PATTERN.matcher(str).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
    }

    /**
     * Create a {@link TypeComment} for a {@link DocumentedNode}'s description string.
     *
     * @param node Documented node containing the description to be processed
     * @return {@link TypeComment}, or empty if the node's description was empty or non-present.
     */
    public static @Nullable TypeComment typeCommentOf(final @NonNull DocumentedNode node) {
        final var description = node.getDescription().orElse("");
        // TODO: isBlank()?
        return description.isEmpty() ? null : () -> typeCommentJavadoc(description);
    }

    @NonNullByDefault
    private static String typeCommentJavadoc(final String description) {
        return replaceAllIllegalChars(formatToParagraph(
            TAIL_COMMENT_PATTERN.matcher(ENTITY_ESCAPER.escape(description)).replaceAll("&#42;&#47;"), 0));
    }

    private static @NonNull String formatToParagraph(final String text, final int nextLineIndent) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        final var sb = new StringBuilder();
        final var lineBuilder = new StringBuilder();
        final var lineIndent = " ".repeat(nextLineIndent);
        final var formattedText = MULTIPLE_SPACES_PATTERN.matcher(NEWLINE_OR_TAB.replaceFrom(text, " "))
                .replaceAll(" ");
        final var tokenizer = new StringTokenizer(formattedText, " ", true);

        boolean isFirstElementOnNewLineEmptyChar = false;
        while (tokenizer.hasMoreElements()) {
            final var nextElement = tokenizer.nextElement().toString();

            if (lineBuilder.length() + nextElement.length() > 80) {
                // Trim trailing whitespace
                for (int i = lineBuilder.length() - 1; i >= 0 && lineBuilder.charAt(i) != ' '; --i) {
                    lineBuilder.setLength(i);
                }

                // Trim leading whitespace
                while (lineBuilder.length() > 0 && lineBuilder.charAt(0) == ' ') {
                    lineBuilder.deleteCharAt(0);
                }

                sb.append(lineBuilder).append('\n');
                lineBuilder.setLength(0);

                if (nextLineIndent > 0) {
                    sb.append(lineIndent);
                }

                if (" ".equals(nextElement)) {
                    isFirstElementOnNewLineEmptyChar = true;
                }
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = false;
            } else {
                lineBuilder.append(nextElement);
            }
        }

        return sb.append(lineBuilder).append('\n').toString();
    }
}
