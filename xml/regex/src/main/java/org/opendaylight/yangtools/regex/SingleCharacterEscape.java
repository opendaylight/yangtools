/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum SingleCharacterEscape implements CharacterClass {
    NEWLINE('\n'),
    RETURN('r'),
    TAB('t'),
    BACKSLASH('\\'),
    PIPE('|'),
    DOT('.'),
    DASH('-'),
    CARET('^'),
    QUESTION('?'),
    STAR('*'),
    PLUS('+'),
    CURLY_LEFT('{'),
    CURLY_RIGHT('}'),
    PAREN_LEFT('('),
    PAREN_RIGHT(')'),
    BRACKET_LEFT('['),
    BRACKET_RIGHT(']');

    private final char ch;

    SingleCharacterEscape(final char ch) {
        this.ch = ch;
    }

    public static SingleCharacterEscape ofLiteral(final String str) {
        return switch (str) {
            case "\\n" -> NEWLINE;
            case "\\r" -> RETURN;
            case "\\t" -> TAB;
            case "\\\\" -> BACKSLASH;
            case "\\|" -> PIPE;
            case "\\." -> DOT;
            case "\\-" -> DASH;
            case "\\^" -> CARET;
            case "\\?" -> QUESTION;
            case "\\*" -> STAR;
            case "\\+" -> PLUS;
            case "\\{" -> CURLY_LEFT;
            case "\\}" -> CURLY_RIGHT;
            case "\\(" -> PAREN_LEFT;
            case "\\)" -> PAREN_RIGHT;
            case "\\[" -> BRACKET_LEFT;
            case "\\]" -> BRACKET_RIGHT;
            default -> throw new IllegalArgumentException("Unhandled value " + str);
        };
    }

    public String toLiteral() {
        return "\\" + ch;
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        sb.append('\\').append(ch);
    }
}
