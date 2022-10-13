/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.antlr;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.parser.antlr.gen.YangStatementLexer;

/**
 * Various utilities related to YANG ABNF.
 */
@NonNullByDefault
public final class GrammarUtils {
    /**
     * This is equivalent to {@link YangStatementLexer#SEP}'s definition. Currently equivalent to the non-repeating part
     * of {@code SEP: [ \n\r\t]+ -> type(SEP);}.
     */
    private static final CharMatcher SEP = CharMatcher.anyOf(" \n\r\t").precomputed();

    /**
     * Splitter corresponding to {@code key-arg} ABNF as defined
     * in <a href="https://tools.ietf.org/html/rfc6020#section-12">RFC6020, section 12</a>:
     *
     * <p>
     * {@code key-arg             = node-identifier *(sep node-identifier)}
     *
     * <p>
     * We also account for {@link #SEP} not handling repetition by ignoring empty strings.
     */
    private static final Splitter KEY_ARG_SPLITTER = Splitter.on(SEP).omitEmptyStrings();

    private GrammarUtils() {
        // Hidden on purpose
    }

    /**
     * Split a string according to {@code key-argb} ABNF production.
     *
     * @param str String to split
     * @return A sequence of strings
     */
    public static Iterable<String> splitKeyArg(final String str) {
        return KEY_ARG_SPLITTER.split(str);
    }
}
