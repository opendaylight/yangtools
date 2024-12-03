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
}
