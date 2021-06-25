/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static org.opendaylight.mdsal.binding.generator.BindingGeneratorUtil.replaceAllIllegalChars;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.TypeComment;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;

/**
 * Utility methods for creating {@link TypeComment}s.
 */
@NonNullByDefault
final class TypeComments {
    private static final Escaper ENTITY_ESCAPER = Escapers.builder()
            .addEscape('<', "&lt;")
            .addEscape('>', "&gt;")
            .addEscape('&', "&amp;")
            .addEscape('@', "&#64;").build();
    private static final Pattern TAIL_COMMENT_PATTERN = Pattern.compile("*/", Pattern.LITERAL);
    private static final CharMatcher NEWLINE_OR_TAB = CharMatcher.anyOf("\n\t");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile(" +");

    private TypeComments() {
        // Hidden on purpose
    }

    /**
     * Create a {@link TypeComment} for a {@link DocumentedNode}'s description string.
     *
     * @param node Documented node containing the description to be processed
     * @return {@link TypeComment}, or empty if the node's description was empty or non-present.
     */
    public static Optional<TypeComment> description(final DocumentedNode node) {
        final String description = node.getDescription().orElse("");
        return description.isEmpty() ? Optional.empty() : Optional.of(() -> replaceAllIllegalChars(
            formatToParagraph(
                TAIL_COMMENT_PATTERN.matcher(ENTITY_ESCAPER.escape(description)).replaceAll("&#42;&#47;"), 0)));
    }

    private static String formatToParagraph(final String text, final int nextLineIndent) {
        if (Strings.isNullOrEmpty(text)) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        final StringBuilder lineBuilder = new StringBuilder();
        final String lineIndent = " ".repeat(nextLineIndent);
        final String formattedText = MULTIPLE_SPACES_PATTERN.matcher(NEWLINE_OR_TAB.replaceFrom(text, " "))
                .replaceAll(" ");
        final StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);

        boolean isFirstElementOnNewLineEmptyChar = false;
        while (tokenizer.hasMoreElements()) {
            final String nextElement = tokenizer.nextElement().toString();

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
