//
// Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
lexer grammar InstanceIdentifierLexer;

DQUOT_START : '"' -> pushMode(DQUOT_STRING_MODE), skip;
SQUOT_START : '\'' -> pushMode(SQUOT_STRING_MODE), skip;

IDENTIFIER : [a-zA-Z][a-zA-Z0-9_\-.]*
  ;

POSITIVE_INTEGER : [1-9][0-9]*
  ;

COLON : ':'
  ;

DOT: '.'
  ;

EQ : '='
  ;

SLASH : '/'
  ;

WSP : [ \t]+
  ;

LBRACKET : '['
  ;

RBRACKET : ']'
  ;

//
// Double-quoted string lexing mode. We do not need to recognize all possible
// escapes here -- just enough not to get confused by runs of backslashes and
// recognize escaped double quotes.
//
mode DQUOT_STRING_MODE;
DQUOT_STRING : (~["\\] | ('\\' .))+
  ;
DQUOT_END : '"' -> popMode;

//
// Single-quoted string lexing mode. We do not interpret anything within single
// quotes.
//
mode SQUOT_STRING_MODE;
SQUOT_STRING : ~[']+
  ;
SQUOT_END : '\'' -> popMode;

