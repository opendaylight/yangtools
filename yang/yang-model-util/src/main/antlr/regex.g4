//
// Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html

// Grammar for parsing https://www.w3.org/TR/xmlschema11-2/#regexs
grammar regex;

@header {
package org.opendaylight.yangtools.yang.model.util.regex;
}

// Regular Expression
regExp : branch ('|' branch)*
    ;

// Branch
branch : piece*
    ;

// Piece
piece : atom quantifier?
    ;

// Quantifier
quantifier : '?' | '*' | '+' | ('{' quantity '}')
    ;
quantity : quantRange | quantMin | QuantExact
    ;
quantRange : QuantExact ',' QuantExact
    ;
quantMin : QuantExact ','
    ;
QuantExact : DIGIT+
    ;

// Atom
atom : NormalChar | charClass | ('(' regExp ')')
    ;

// Normal Character
NormalChar : ~('.' | '\\' | '?' | '*' | '+' | '{' | '}' | '(' | ')' | '|' | '[' | ']')
    ;

// Character Class
charClass : SingleCharEsc | charClassEsc | charClassExpr | WildcardEsc
    ;

// Character Class Expression
charClassExpr : '[' charGroup ']'
    ;

// Character Group
charGroup : (posCharGroup | negCharGroup) ('-' charClassExpr)?
    ;

// Positive Character Group
posCharGroup : (charGroupPart)+
    ;

// Negative Character Group
negCharGroup : '^' posCharGroup
    ;

// Character Group Part
charGroupPart : singleChar | charRange | charClassEsc 
    ;
singleChar : SingleCharEsc | SingleCharNoEsc
    ;

// Character Range
charRange : singleChar '-' singleChar
    ;

// Single Unescaped Character
SingleCharNoEsc : ~('\\' | '[' | ']')
    ;

// Character Class Escape
charClassEsc : (MultiCharEsc | catEsc | complEsc)
    ;

// Single Character Escape
SingleCharEsc : '\\' [nrt|.?*+(){}\u002D\u005B\u005D\u005E]
    ;

// Category Escape
catEsc : '\\p{' charProp '}'
    ;
complEsc : '\\P{' charProp '}'
    ;
charProp : IsCategory | IsBlock
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
IsBlock : 'Is' ([a-zA-Z] | DIGIT | DASH)+
    ;

// Multi-Character Escape
MultiCharEsc : '\\' [sSiIcCdDwW]
    ;
WildcardEsc : '.'
    ;

fragment DIGIT : '0'..'9'
    ;
fragment DASH : '-'
    ;

