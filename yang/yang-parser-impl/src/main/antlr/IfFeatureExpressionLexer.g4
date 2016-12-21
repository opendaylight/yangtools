//
// Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
lexer grammar IfFeatureExpressionLexer;

@header {
package org.opendaylight.yangtools.antlrv4.code.gen;
}

NOT : 'not';
LP : '(';
RP : ')';
AND : 'and';
OR : 'or';
COLON : ':';
SEP: [ \n\r\t]+;
IDENTIFIER : [a-zA-Z][a-zA-Z0-9_-]*;
