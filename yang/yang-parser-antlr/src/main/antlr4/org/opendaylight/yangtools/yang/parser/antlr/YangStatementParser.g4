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

// NOTE: we need to use SEP*/SEP+ because comments end up breaking whitespace
//       sequences into two.
file : SEP* statement SEP* EOF;
statement : keyword (SEP+ argument)? SEP* (SEMICOLON | LEFT_BRACE SEP* (statement SEP*)* RIGHT_BRACE);
keyword : IDENTIFIER (COLON IDENTIFIER)?;

// A statement argument can either be unquoted string, or a quoted string,
// or a concatenation of quoted strings.
argument : unquoted | (quoted (concat)*);

// A quoted string, which is quite simple
quoted : DQUOT_STRING | SQUOT_STRING | EMPTY_QUOT;

// And additional concatenation of a quoted string
concat : SEP* PLUS SEP* quoted;

// An unquoted string, which can be a lot of things
unquoted : (IDENTIFIER | COLON | PLUS | YANG_CHAR)+;

