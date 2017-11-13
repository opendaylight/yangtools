//
// Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
parser grammar YangStatementParser;

@header {
package org.opendaylight.yangtools.antlrv4.code.gen;
}

options{
    tokenVocab = YangStatementLexer;
}

statement : SEP* keyword SEP* (argument)? SEP* (SEMICOLON | LEFT_BRACE SEP* (statement)* SEP* RIGHT_BRACE SEP*) SEP*;
keyword : (IDENTIFIER COLON)? IDENTIFIER;

argument : STRING (SEP* PLUS SEP* STRING)* | IDENTIFIER;