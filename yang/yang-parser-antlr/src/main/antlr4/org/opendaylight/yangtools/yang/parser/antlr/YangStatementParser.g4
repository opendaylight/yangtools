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

// Alright, so what constitutes a string is rather funky. We need to deal with
// the flaky definitions of RFC6020, which allow for insane quoting as well as
// exclusion of comments. We also need to allow for stitching back tokens like
// PLUS/COLON, which may end up being valid identifiers. Finally we need to allow
// IDENTIFIER to be concatenated back to a string
argument :
    // Plain spans of we have exluded during tokenization
    COLON+ | PLUS+ | STAR+ | SLASH+
    |
    // A concatenation of quoted strings
    concatenation
    // FIXME: a crapload of other options
    ;

// A potential concatenation of quoted strings. Note we do not strip quote tokens
// so as to be able to handle empty strings.
concatenation : quoted_string (SEP* PLUS SEP* quoted_string)*;
quoted_string : dquot_string | squot_string;
dquot_string : DQUOT_START DQUOT_STRING? DQUOT_END;
squot_string : SQUOT_START SQUOT_STRING? SQUOT_END;

