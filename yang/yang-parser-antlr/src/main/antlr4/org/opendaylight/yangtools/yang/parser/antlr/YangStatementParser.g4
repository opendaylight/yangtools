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
    // A concatenation of quoted strings
    concatenation
    // Plain spans of we have exluded during tokenization
    COLON+ | PLUS+ | STAR+ | SLASH+
    |
    // FIXME: a crapload of other options, which just need to deal with various
    //        lexing combinations. We need to exclude 'SLASH SLASH', 'SLASH STAR'
    //        and 'STAR SLASH' combinations -- somehow. We probably want to create
    //        safe/start/end groups for both star and slash and then express recursive
    //        matches, so that we never see the forbidden combinations.
    ;

// A potential concatenation of quoted strings. Note we do not strip quote tokens
// so as to be able to handle empty strings.
concatenation : quoted_string (SEP* PLUS SEP* quoted_string)*;
quoted_string :
    DQUOT_START DQUOT_STRING? DQUOT_END
    |
    SQUOT_START SQUOT_STRING? SQUOT_END
    ;

