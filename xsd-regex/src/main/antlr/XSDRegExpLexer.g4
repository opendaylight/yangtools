//
// Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
lexer grammar XSDRegExpLexer;

@header {
package org.opendaylight.yangtools.xsd.regex.impl;
}

LP : '(';
RP : ')';
PLUS : '+';
QUESTION_MARK : '?';
COMMA : ',';
STAR : '*';
LCB : '{';
RCB : '}';
PIPE: '|';
LSB: '[';
RSB: ']';
CARPET: '^';
MINUS: '-';


QuantExact : [0-9]+;
CHAR_T : ~('.' | '\\' | '?' | '*' | '+' | '(' | ')' | '[' | ']');

XmlChar : ~('\\' | '-' | '[' | ']');
XmlCharIncDash : ~('\\' | '[' | ']');

SingleCharEsc : '\\' [nrt\|.?*+(){}#x2D#x5B#x5D#x5E];

CatEsc : '\\p{' CharProp '}';
ComplEsc : '\\P{' CharProp '}';
CharProp : IsCategory | IsBlock;

IsCategory : Letters | Marks | Numbers | Punctuation | Separators | Symbols | Others;
Letters : 'L' [ultmo]?;
Marks : 'M' [nce]?;
Numbers : 'N' [dlo]?;
Punctuation : 'P' [cdseifo]?;
Separators : 'Z' [slp]?;
Symbols : 'S' [mcko]?;
Others : 'C' [cfon]?;

IsBlock : 'Is' [a-zA-Z0-9\-]+;

MultiCharEsc : '\\' [sSiIcCdDwW];
WildcardEsc :  '.';