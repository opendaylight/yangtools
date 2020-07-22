//
// Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
parser grammar YangStatementParser;

options {
    tokenVocab = YangStatementLexer;
}

statement : SEP* keyword SEP* (argument)? SEP* (SEMICOLON | LEFT_BRACE SEP* (statement)* SEP* RIGHT_BRACE SEP*) SEP*;
keyword : (IDENTIFIER COLON)? IDENTIFIER;

// A statement argument can either be unquoted string, or a quoted string,
// or a concatenation of quoted strings.
argument : unquoted | (quoted (SEP* PLUS SEP* quoted)*);

// A quoted string, which is quite simple
quoted : DQUOT_STRING | SQUOT_STRING | EMPTY_QUOT;

// An unquoted string, which can be a lot of things
unquoted : (IDENTIFIER | COLON | PLUS | STRING)+;

