//
// Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html

// Parser grammar for https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#regexs
parser grammar regexParser;
options { tokenVocab = regexLexer; }

// This grammar is modified in following ways:
// - charGroup definition inlines the charClassSub case
//   This allows us to simplify processing, eliminating one level of nesting. It also
//   makes this rule consistent with XSD 1.1 definition.
// - charRange and atom are modified to not use Char, XmlChar and XmlCharIncDash, but
//   rely on xmlChar rule and explicit composition instead.

@header {
package org.opendaylight.yangtools.yang.model.util.regex;
}

// Regular Expression
regExp : branch (PIPE branch)*
    ;

// Branch
branch : piece*
    ;

// Piece
piece : atom quantifier?
    ;

// Quantifier
quantifier : QUESTION | STAR | PLUS | StartQuantity quantity EndQuantity
    ;
quantity : quantRange | quantMin | QuantExact
    ;
quantRange : QuantExact COMMA QuantExact
    ;
quantMin : QuantExact COMMA
    ;

// Atom
atom : CharNoDash | DASH | charClass | (LPAREN regExp RPAREN)
    ;

// Character Class
charClass : charClassEsc | charClassExpr | WildcardEsc
    ;

// Character Class Expression
charClassExpr : LBRACKET charGroup RBRACKET
    ;

// Character Group, with Character Class Subtraction inlined for simplicity
charGroup :  (posCharGroup | negCharGroup) (DASH charClassExpr)?
    ;

// Positive Character Group
posCharGroup : (charRange | charClassEsc)+
    ;

// Negative Character Group
negCharGroup : CARET posCharGroup
    ;

// Character Range
charRange : seRange | xmlChar | DASH
    ;
seRange : charOrEsc DASH charOrEsc
    ;
charOrEsc : xmlChar | SingleCharEsc
    ;

// XmlChar composed at parser level
xmlChar : CharNoDash | WildcardEsc | BACKSLASH | QUESTION | STAR | PLUS | LPAREN | RPAREN | PIPE
    ;

// Character Class Escape
charClassEsc : SingleCharEsc | MultiCharEsc | catEsc | complEsc
    ;

// Category Escape
catEsc : CatEsc charProp EndCategory
    ;
complEsc : ComplEsc charProp EndCategory
    ;
charProp : IsCategory | IsBlock
    ;

