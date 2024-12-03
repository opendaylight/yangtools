/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xsd;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum MultiCharacterEscape implements CharacterClass {
    // XSD:  [ \t\n\r]
    // Java: [ \t\n\x0B\f\r]
    SPACE('s'),
    NOT_SPACE('S'),

    // XSD:  Letter | '_' | ':'
    // Java translated
    IDENT('i'),
    NOT_IDENT('I'),

    // XSD:  NameChar
    // Java translated
    CHAR('c'),
    NOT_CHAR('C'),

    // XSD:  \p{Nd}
    // Java: [0-9]
    DIGIT('d'),
    NOT_DIGIT('D'),

    // XSD:  [#x0000-#x10FFFF]-[\p{P}\p{Z}\p{C}]
    // Java: [a-zA-Z_0-9]
    WORD('w'),
    NOT_WORD('W');

    private final char ch;

    MultiCharacterEscape(final char ch) {
        this.ch = ch;
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
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }
}
