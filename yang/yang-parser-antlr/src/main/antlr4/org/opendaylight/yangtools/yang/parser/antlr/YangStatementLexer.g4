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
    SLASH,
    STAR,
    DQUOT_STRING,
    SQUOT_STRING,
    UQUOT_STRING
}

SEMICOLON : ';' -> type(SEMICOLON);
LEFT_BRACE : '{' -> type(LEFT_BRACE);
RIGHT_BRACE : '}' -> type(RIGHT_BRACE);
COLON : ':' -> type(COLON);
PLUS : '+' -> type(PLUS);

// RFC6020 section 6.1.1:
//   Comments are C++ style.  A single line comment starts with "//" and
//   ends at the end of the line.  A block comment is enclosed within "/*"
//   and "*/".
//
// RFC7950 section 6.1.1:
//   Comments are C++ style.  A single line comment starts with "//" and
//   ends at the end of the line.  A block comment starts with "/*" and
//   ends with the nearest following "*/".
//
//   Note that inside a quoted string (Section 6.1.3), these character
//   pairs are never interpreted as the start or end of a comment.
//
// What constitutes 'end of the line' is not specified in RFC7950, hence
// we are using RFC7950-clarified definition.
LINE_COMMENT : '//' .*? '\r'? ('\n' | EOF) -> skip;
BLOCK_COMMENT : '/*' .*? '*/' -> skip;

SEP: [ \n\r\t]+ -> type(SEP);

// Special-cased identifier string
IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_\-.]* -> type(IDENTIFIER);

// RFC6020 section 6.1.3:
//   If a string contains any space or tab characters, a semicolon (";"),
//   braces ("{" or "}"), or comment sequences ("//", "/*", or "*/"), then
//   it MUST be enclosed within double or single quotes.
//
// RFC7950 section 6.1.3:
//   An unquoted string is any sequence of characters that does not
//   contain any space, tab, carriage return, or line feed characters, a
//   single or double quote character, a semicolon (";"), braces ("{" or
//   "}"), or comment sequences ("//", "/*", or "*/").
//
// Since we need tokenization to work in both worlds, we are taking only
// RFC6020 with CR/LF clarifications -- and allow quotes to appear in the body
// of a string. We additionally exclude COLON, so as to prefer IDENTIFIER
// tokenization -- which allows us to make keyword work properly.
//
// Furthermore we need to exclude PLUS so that concatenation works as expected
// when + is not separated by whitespace -- even RFC7950 is far from being
// well-specified in this regard.
//
// The most problematic here is the comment sequence exclusion, as we cannot
// just exclude it from productions. We therefore provide single-char
// tokenizations of both '*' and '/', and deal with them separately in the
// parser
SLASH : '/' -> type(SLASH);
STAR : '*' -> type(STAR);
UQUOT_STRING :
    // Any eager span that does not start with single/double quote and does not
    // have slash/star.
    ~([ \n\r\t] | [;{}:+] | [/*] | ['"])
    ~([ \n\r\t] | [;{}:+] | [/*])*
    -> type(UQUOT_STRING);

// Double/single-quoted strings. We deal with these using specialized modes.
DQUOT_START : '"' -> pushMode(DQUOT_STRING_MODE);
SQUOT_START : '\'' -> pushMode(SQUOT_STRING_MODE);

//
// Double-quoted string lexing mode. We do not need to recognize all possible
// escapes here -- just enough not to get confused by runs of backslashes and
// recognize escaped double quotes.
//
mode DQUOT_STRING_MODE;
DQUOT_STRING : (~["\\] | ('\\' .))+ -> type(DQUOT_STRING);
DQUOT_END : '"' -> popMode;

//
// Single-quoted string lexing mode. We do not interpret anything within single
// quotes.
//
mode SQUOT_STRING_MODE;
SQUOT_STRING : ~[']+ -> type(SQUOT_STRING);
SQUOT_END : '\'' -> popMode;

