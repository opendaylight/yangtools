lexer grammar instanceIdentifierLexer;

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
COLON : ':' ;
DOT : '.' ;
EQ : '=' ;
LBRACKET : '[' ;
RBRACKET : ']' ;
SLASH : '/' ;

Identifier : [a-zA-Z][a-zA-Z0-9_\-.]*
  ;

PositiveIntegerValue : [1-9][0-9]*
  ;

WSP : [ \t]+
  ;

// Double/single-quoted strings. We deal with these using specialized modes.
DQUOT_START : '"' -> pushMode(DQUOT_STRING_MODE), skip;
SQUOT_START : '\'' -> pushMode(SQUOT_STRING_MODE), skip;

//
// Double-quoted string lexing mode. We do not need to recognize all possible
// escapes here -- just enough not to get confused by runs of backslashes and
// recognize escaped double quotes.
//
mode DQUOT_STRING_MODE;
DQUOT_STRING : (YANGCHAR | '\'' | ('\\' [nt"\\]))+ ;
DQUOT_END : '"' -> popMode;

//
// Single-quoted string lexing mode. We do not interpret anything within single
// quotes.
//
mode SQUOT_STRING_MODE;
SQUOT_STRING : (YANGCHAR | '"' | '\\')+ ;
SQUOT_END : '\'' -> popMode;

fragment
YANGCHAR : '\t'..'\n'
  | '\r'

  // | '\u0020'..'\uD7FF' without "'", '"' and '\'
  | '\u0020'..'\u0021' // 0x22 = "
  | '\u0023'..'\u0026' // 0x27 = '
  | '\u0028'..'\u005B' // 0x5C = \
  | '\u005D'..'\uD7FF'

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

