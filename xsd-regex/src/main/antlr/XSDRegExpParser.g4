//
// Copyright (c) 2016 Pantheon Technologies, s.r.o. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//

// Grammar according to https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#regexs
parser grammar XSDRegExpParser;

@header {
package org.opendaylight.yangtools.xsd.regex.impl;
}

options{
    tokenVocab = XSDRegExpLexer;
}

regExp : branch ( PIPE branch )*;
branch : (piece)*;
piece: atom (quantifier)?;

quantifier : (QUESTION_MARK | STAR | PLUS) | ( LCB quantity RCB );
quantity : quantRange | quantMin | QuantExact;
quantRange : QuantExact COMMA QuantExact;
quantMin : QuantExact COMMA;

atom : charRule | charClass | LP regExp RP;

charRule: CARPET | CHAR_T;

charClass : charClassEsc | charClassExpr | WildcardEsc;

charClassExpr : LSB charGroup RSB;

charGroup : negCharGroup | posCharGroup  | charClassSub;

posCharGroup : ( charRange | charClassEsc )+;

negCharGroup : CARPET posCharGroup;

charClassSub : ( posCharGroup | negCharGroup ) MINUS charClassExpr;

charRange : seRange | XmlCharIncDash;
seRange : charOrEsc MINUS charOrEsc;
charOrEsc : XmlChar | SingleCharEsc;

charClassEsc : ( SingleCharEsc | MultiCharEsc | CatEsc | ComplEsc );



