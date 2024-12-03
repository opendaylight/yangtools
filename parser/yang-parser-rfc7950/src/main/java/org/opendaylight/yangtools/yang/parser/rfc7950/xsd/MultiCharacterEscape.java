/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xsd;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum MultiCharacterEscape implements CharacterClass {
    DOT("."),

    // XSD:  [ \t\n\r]
    // Java: [ \t\n\x0B\f\r]
    S('s'),
    NOT_S('S'),

    // XSD:  Letter | '_' | ':'
    // Java translated
    I('i'),
    NOT_I('I'),

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

    private final String str;

    MultiCharacterEscape(final String str) {
        this.str = requireNonNull(str);
    }

    MultiCharacterEscape(final char c) {
        this("\\" + c);
    }
}
