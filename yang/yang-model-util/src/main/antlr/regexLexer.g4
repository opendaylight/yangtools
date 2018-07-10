//
// Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html

// Lexer grammar for https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#regexs
lexer grammar regexLexer;

// This grammar is modified in following ways:
// - we use lexer modes to disambiguate between Char, XmlChar and QuantExact
// - we use separate lexer tokens to disambiguate positive and negative character groups
// - XmlCharIncDash is removed in favor of DASH token, which is handled in parser

@header {
package org.opendaylight.yangtools.yang.model.util.regex;
}

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
Char : ~('.' | '\\' | '?' | '*' | '+' | '(' | ')' | '|' | '[' | ']')
    ;

// Quantifier's quantity rule support
StartQuantity : '{' -> pushMode(QUANTITY)
    ;

// Single Character Escape
SingleCharEsc : SINGLE_ESC
    ;

// Multi-Character Escape
MultiCharEsc : MULTI_ESC
    ;

// Category Escape
CatEsc : CAT_ESC -> pushMode(CATEGORY)
    ;
ComplEsc : COMPL_ESC -> pushMode(CATEGORY)
    ;

// Positive/Negative Character Group
NegCharGroup : '[^' -> pushMode(CHARGROUP)
    ;
PosCharGroup : '[' -> pushMode(CHARGROUP)
    ;

mode QUANTITY;
EndQuantity : '}' -> popMode
    ;
QuantExact : [0-9]+
    ;
COMMA : ','
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

mode CHARGROUP;
NestedSingleCharEsc : SINGLE_ESC
    ;
NestedMultiCharEsc : MULTI_ESC
    ;
NestedCatEsc : CAT_ESC -> pushMode(CATEGORY)
    ;
NestedComplEsc : COMPL_ESC -> pushMode(CATEGORY)
    ;
NestedNegCharGroup : '[^' -> pushMode(CHARGROUP)
    ;
NestedPosCharGroup : '[' -> pushMode(CHARGROUP)
    ;
EndCharGroup : ']' -> popMode
    ;
DASH : '-'
    ;
XmlChar : ~('-' | '[' | ']')
    ;

fragment CAT_ESC : '\\p{'
    ;
fragment COMPL_ESC : '\\P{'
    ;
fragment MULTI_ESC : '\\' [sSiIcCdDwW]
    ;
fragment SINGLE_ESC : '\\' [nrt\\|.?*+(){}\u002D\u005B\u005D\u005E]
    ;

