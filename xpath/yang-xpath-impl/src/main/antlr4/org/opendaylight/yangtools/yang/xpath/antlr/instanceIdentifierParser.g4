parser grammar instanceIdentifierParser;

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
options {
    tokenVocab = instanceIdentifierLexer;
}

instanceIdentifier : (SLASH pathArgument)+ EOF
  ;

pathArgument : nodeIdentifier predicate?
  ;

nodeIdentifier : Identifier COLON Identifier
  ;

predicate : keyPredicate+
  | leafListPredicate
  | pos
  ;

keyPredicate : LBRACKET WSP? keyPredicateExpr WSP? RBRACKET
  ;

keyPredicateExpr : nodeIdentifier eqQuotedString
  ;

leafListPredicate : LBRACKET WSP? leafListPredicateExpr WSP? RBRACKET
  ;

leafListPredicateExpr : DOT eqQuotedString
  ;

// Common tail of leafListPredicateExpr and keyPredicateExpr
eqQuotedString : WSP? EQ WSP? quotedString
  ;

pos : LBRACKET WSP? PositiveIntegerValue WSP? RBRACKET
  ;

quotedString : SQUOT_STRING? SQUOT_END
  | DQUOT_STRING? DQUOT_END
  ;

