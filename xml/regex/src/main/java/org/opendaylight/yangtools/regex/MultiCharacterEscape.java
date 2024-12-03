/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum MultiCharacterEscape implements CharacterClass {
    // Java: [ \t\n\x0B\f\r]
    SPACE('s', "[ \\t\\n\\r]"),
    NOT_SPACE('S', "[^ \\t\\n\\r]"),

    // XSD:  Letter | '_' | ':'
    // Java translated
    IDENT('i', null),
    NOT_IDENT('I', null),

    // XSD:  NameChar
    // Java translated
    CHAR('c', null),
    NOT_CHAR('C', null),

    // Java: [0-9]
    DIGIT('d', "\\p{Nd}"),
    NOT_DIGIT('D', "\\P{Nd}"),

    // XSD:  [#x0000-#x10FFFF]-[\p{P}\p{Z}\p{C}]
    // Java: [a-zA-Z_0-9]
    WORD('w', null),
    NOT_WORD('W', null);

    private final String fragment;
    private final char ch;

    MultiCharacterEscape(final char ch, final String fragment) {
        this.ch = ch;
        this.fragment = requireNonNull(fragment);
    }

    // Note: does not handle "dot"
    public static MultiCharacterEscape ofLiteral(final String str) {
        return switch (str) {
            case "\\i" -> IDENT;
            case "\\I" -> NOT_IDENT;
            case "\\c" -> CHAR;
            case "\\C" -> NOT_CHAR;
            case "\\d" -> DIGIT;
            case "\\D" -> NOT_DIGIT;
            case "\\w" -> WORD;
            case "\\W" -> NOT_WORD;
            default -> throw new IllegalArgumentException("Unhandled value " + str);
        };
    }

    public String toLiteral() {
        return "\\" + ch;
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        sb.append(fragment);
    }
}
