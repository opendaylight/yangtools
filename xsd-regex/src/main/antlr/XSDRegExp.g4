//
// Copyright (c) 2016 Pantheon Technologies, s.r.o. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//

// Grammar according to https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#regexs
grammar XSDRegExp;

@header {
package org.opendaylight.yangtools.xsd.regex.impl;
}

regExp : branch ( '|' branch )*;
branch : piece*;
piece: atom quantifier?;

quantifier : ('?' | '*' | '+') | ( '{' quantity '}' );
quantity : quantRange | quantMin | QuantExact;
quantRange : QuantExact ',' QuantExact;
quantMin : QuantExact ',';
QuantExact : [0-9]+;

atom : Char | charClass | ( '(' regExp ')' );

Char : ~('.' | '\\' | '?' | '*' | '+' | '(' | ')' | '[' | ']');

charClass : charClassEsc | charClassExpr | WildcardEsc;

charClassExpr : '[' charGroup ']';

charGroup : posCharGroup | negCharGroup | charClassSub;

posCharGroup : ( charRange | charClassEsc )+;

negCharGroup : '^' posCharGroup;

charClassSub : ( posCharGroup | negCharGroup ) '-' charClassExpr;

charRange : seRange | XmlCharIncDash;
seRange : charOrEsc '-' charOrEsc;
charOrEsc : XmlChar | SingleCharEsc;
XmlChar : ~('\\' | '-' | '[' | ']');
XmlCharIncDash : ~('\\' | '[' | ']');

charClassEsc : ( SingleCharEsc | MultiCharEsc | catEsc | complEsc );

SingleCharEsc : '\\' [nrt\|.?*+(){}#x2D#x5B#x5D#x5E];

catEsc : '\\p{' charProp '}';
complEsc : '\\P{' charProp '}';
charProp : IsCategory | IsBlock;

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

