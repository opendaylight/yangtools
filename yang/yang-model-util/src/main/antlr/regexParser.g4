//
// Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html

// Parser grammar for https://www.w3.org/TR/xmlschema11-2/#regexs
parser grammar regexParser;

options { tokenVocab = regexLexer; }

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
atom : NormalChar | charClass | (LPAREN regExp RPAREN)
    ;

// Character Class
charClass : SingleCharEsc | charClassEsc | charClassExpr | WildcardEsc
    ;

// Character Class Expression
charClassExpr : LBRACKET charGroup RBRACKET
    ;

// Character Group
charGroup : (posCharGroup | negCharGroup) (DASH charClassExpr)?
    ;

// Positive Character Group
posCharGroup : (charGroupPart)+
    ;

// Negative Character Group
negCharGroup : CARET posCharGroup
    ;

// Character Group Part
charGroupPart : singleChar | charRange | charClassEsc 
    ;

// Character Range
charRange : singleChar DASH singleChar
    ;

singleChar : SingleCharEsc | SingleCharNoEsc | NormalChar
    ;

// Character Class Escape
charClassEsc : MultiCharEsc | catEsc | complEsc
    ;

// Category Escape
catEsc : CatEsc charProp EndCategory
    ;
complEsc : ComplEsc charProp EndCategory
    ;
charProp : IsCategory | IsBlock
    ;

