//
// Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html

// Lexer grammar for https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#regexs
lexer grammar regexLexer;

// This grammar is modified in following ways:
// - Char, XmlChar and XmlCharIncDash have been removed to disambiguate tokenization.
//   We provide CharNoDash, and separate single-character tokens, with parser grouping
//   these tokens.

@header {
package org.opendaylight.yangtools.yang.model.util.regex;
}

CARET : '^'
    ;

DASH : '-'
    ;

LBRACKET: '['
    ;

RBRACKET: ']'
    ;

LPAREN : '('
    ;

RPAREN : ')'
    ;

PIPE : '|'
    ;

PLUS : '+'
    ;

QUESTION : '?'
    ;

STAR : '*'
    ;

BACKSLASH : '\\'
    ;

WildcardEsc : '.'
    ;

// Quantifier's quantity rule support
StartQuantity : '{' -> pushMode(QUANTITY)
    ;

// Single Character Escape
SingleCharEsc : '\\' [nrt\\|.?*+(){}\u002D\u005B\u005D\u005E]
    ;

// Multi-Character Escape
MultiCharEsc : '\\' [sSiIcCdDwW]
    ;
// Category Escape
CatEsc : '\\p{' -> pushMode(CATEGORY)
    ;
ComplEsc : '\\P{' -> pushMode(CATEGORY)
    ;

// Production 10 Char without '-'
CharNoDash : ~('.' | '\\' | '?' | '*' | '+' | '(' | ')' | '|' | '[' | ']' | '-')
    ;

mode QUANTITY;
QuantExact : [0-9]+
    ;
COMMA : ','
    ;
EndQuantity : '}' -> popMode
    ;

mode CATEGORY;
EndCategory : '}' -> popMode
    ;

// Categories
IsCategory : Letters | Marks | Numbers | Punctuation | Separators | Symbols | Others
    ;
Letters : 'L' [ultmo]?
    ;
Marks : 'M' [nce]?
    ;
Numbers : 'N' [dlo]?
    ;
Punctuation : 'P' [cdseifo]?
    ;
Separators : 'Z' [slp]?
    ;
Symbols : 'S' [mcko]?
    ;
Others : 'C' [cfon]?
    ;

// Block Escape
IsBlock : 'Is' ([a-z0-9A-Z] | '-')+
    ;

