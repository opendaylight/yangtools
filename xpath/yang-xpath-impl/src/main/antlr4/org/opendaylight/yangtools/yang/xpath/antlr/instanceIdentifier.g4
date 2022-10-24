grammar instanceIdentifier;

/*
 * YANG 1.1 instance-identifier grammar, as defined in
 * https://tools.ietf.org/html/rfc7950#section-9.13
 *
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
instanceIdentifier : ('/' pathArgument)+
  ;

pathArgument : nodeIdentifier predicate?
  ;

nodeIdentifier : Identifier (':' Identifier)?
  ;

predicate : keyPredicate+
  | leafListPredicate
  | pos
  ;

keyPredicate : '[' WSP? keyPredicateExpr WSP? ']'
  ;

keyPredicateExpr  : nodeIdentifier eqQuotedString
  ;

leafListPredicate : '[' WSP? leafListPredicateExpr WSP? ']'
  ;

leafListPredicateExpr : '.' eqQuotedString
  ;

// Common tail of leafListPredicateExpr and keyPredicateExpr
eqQuotedString : WSP? '=' WSP? quotedString
  ;

pos : '[' WSP? PositiveIntegerValue WSP? ']'
  ;

quotedString : '\'' STRING '\''
  | '"' STRING '"'
  ;

Identifier : [a-zA-Z][a-zA-Z0-9_\-.]*
  ;

PositiveIntegerValue : [1-9][0-9]*
  ;

STRING : YANGCHAR+
  ;

WSP : [ \t]+
  ;

fragment
YANGCHAR : '\t'..'\n'
  | '\r'
  | '\u0020'..'\uD7FF'
  | '\uE000'..'\uFDCF'
  | '\uFDF0'..'\uFFFD'
  | '\u{10000}'..'\u{1FFFD}'
  | '\u{20000}'..'\u{2FFFD}'
  | '\u{30000}'..'\u{3FFFD}'
  | '\u{40000}'..'\u{4FFFD}'
  | '\u{50000}'..'\u{5FFFD}'
  | '\u{60000}'..'\u{6FFFD}'
  | '\u{70000}'..'\u{7FFFD}'
  | '\u{80000}'..'\u{8FFFD}'
  | '\u{90000}'..'\u{9FFFD}'
  | '\u{A0000}'..'\u{AFFFD}'
  | '\u{B0000}'..'\u{BFFFD}'
  | '\u{C0000}'..'\u{CFFFD}'
  | '\u{D0000}'..'\u{DFFFD}'
  | '\u{E0000}'..'\u{EFFFD}'
  | '\u{F0000}'..'\u{FFFFD}'
  | '\u{100000}'..'\u{10FFFD}'
  ;

