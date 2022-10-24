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

nodeIdentifier : IDENTIFIER ( ':' IDENTIFIER )?
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
eqQuotedString : WSP? '=' WSP? QUOTED_STRING
  ;

pos : '[' WSP? POS_INDEX WSP? ']'
  ;

QUOTED_STRING :  '"' (~('"' | '\\' ) | '\\' ('"' | '\\'))* '"'
  | '\'' (~('\'' | '\\' ) | '\\' ('\'' | '\\'))* '\''
  ;

IDENTIFIER : [a-zA-Z][a-zA-Z0-9_\-.]*
  ;

POS_INDEX : '0' | [1-9][0-9]*
  ;

WSP : [ \t]+
  ;

