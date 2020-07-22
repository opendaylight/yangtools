//
// Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
lexer grammar YangStatementLexer;

tokens {
    SEMICOLON,
    LEFT_BRACE,
    RIGHT_BRACE,
    SEP,
    IDENTIFIER,
    COLON,
    PLUS,
    // Interior of a double-quoted string (without double quotes)
    DQUOT_STRING,
    // Interior of a single-quoted string (without single quotes)
    SQUOT_STRING,
    // An quoted string
    EMPTY_QUOT,
    // Any other concatenation of YANG characters
    STRING
}

// Basic lexical items, cannot occur without quoting, which is handled in separate
// modes
SEMICOLON : ';' -> type(SEMICOLON);
LEFT_BRACE : '{' -> type(LEFT_BRACE);
RIGHT_BRACE : '}' -> type(RIGHT_BRACE);

// Simple line comment. No need for a mode.
// FIXME: this looks like a bit of SEP magic before and aft, revisit this
LINE_COMMENT :  [ \n\r\t]* ('//' (~[\r\n]*)) [ \n\r\t]* -> skip;

// Multi-line comment. Enter a new parsing mode to handle that
BLOCK_COMMENT_START : '/*' -> pushMode(BLOCK_COMMENT_MODE), skip;

// Double/single quotes. Handled by dedicated modes, except empty strings, which
// have their own token.
EMPTY_QUOT : ('""' | '\'\'') -> type(EMPTY_QUOT);
DQUOT_START : '"' -> pushMode(DQUOT_STRING_MODE);
SQUOT_START : '\'' -> pushMode(SQUOT_STRING_MODE);

// At least one separator. Merge them at lexer level.
SEP: [ \n\r\t]+ -> type(SEP);

// String simple enough to be an unquoted 'identifier' ABNF production
IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_\-.]* -> type(IDENTIFIER);

// Colon is special, as it can be part of a 'keyword' parser construct, or it can
// be part of 'argument'. The parser will need to disambiguate this.
COLON : ':' -> type(COLON);

// Plus is special, as it can be part of a string concatenation, or it can be part
// of 'argument'. The parser will need to disambiguate this.
PLUS : '+' -> type(PLUS);

// Leftover characters, excluding characters which are themselves tokens. Allowed
// recombinations are handled by the parser.
STRING : YANG_CHAR+ -> type(STRING);

// FIXME: this is all characters without the special cases. Turn this into specific
//        ranges as per RFC7950, excluding the special cases.
fragment YANG_CHAR : (~[;{}"':+])+;

//
// Block comment lexing mode. Looks for the first '*/' and ignores
// everything else.
//
mode BLOCK_COMMENT_MODE;
BLOCK_COMMENT_END : '*/' -> popMode, skip;
BLOCK_COMMENT :  . -> skip;

//
// Double-quoted string lexing mode. We do not need to recognize all possible
// escapes here -- just enough not to get confused by runs of backslashes and
// recognize escaped double quotes.
//
mode DQUOT_STRING_MODE;
DQUOT_STRING : (~["\\] | '\\\n' | '\\\\t' | '\\\\"' | '\\\\')+ -> type(DQUOT_STRING);
DQUOT_END : '"' -> popMode;

//
// Single-quoted string lexing mode. 
//
mode SQUOT_STRING_MODE;
SQUOT_STRING : ~[']+ -> type(SQUOT_STRING);
SQUOT_END : '\'' -> popMode;

