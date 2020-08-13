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
argument : unquotedString | quotedString (SEP* PLUS SEP* quotedString)*;
unquotedString : SLASH | STAR+ | (SLASH? | STAR*) stringPart+ (SLASH? | STAR*);

// A string which is guaranteed to not have slash/star in either start or end
// and can thus be concatenated without allowing '/*', '//' and '*/' to appear.
stringPart:
    (IDENTIFIER | COLON | PLUS | UQUOT_STRING)+
    |
    stringPart SLASH stringPart
    |
    stringPart STAR+ stringPart
    ;

quotedString :
    DQUOT_START DQUOT_STRING? DQUOT_END
    |
    SQUOT_START SQUOT_STRING? SQUOT_END
    ;

