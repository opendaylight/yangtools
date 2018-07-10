//
// Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html

// Lexer grammar for https://www.w3.org/TR/xmlschema11-2/#regexs
// FIXME: this does not really work, we need a modal lexer
lexer grammar regexLexer;

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

WildcardEsc : '.'
    ;

// Quantifier's quantity rule support
StartQuantity : '{' -> pushMode(QUANTITY)
    ;

// Single Character Escape
SingleCharEsc : '\\' [nrt|.?*+(){}\u002D\u005B\u005D\u005E]
    ;

// Multi-Character Escape
MultiCharEsc : '\\' [sSiIcCdDwW]
    ;
// Category Escape
CatEsc : '\\p{' -> pushMode(CATEGORY)
    ;
ComplEsc : '\\P{' -> pushMode(CATEGORY)
    ;

// Normal Character
NormalChar : ~('.' | '\\' | '?' | '*' | '+' | '{' | '}' | '(' | ')' | '|' | '[' | ']')
    ;

// Single Unescaped Character
SingleCharNoEsc : ~('\\' | '[' | ']')
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

