//
// Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
parser grammar IfFeatureExpressionParser;

@header {
package org.opendaylight.yangtools.antlrv4.code.gen;
}

options{
    tokenVocab = IfFeatureExpressionLexer;
}


if_feature_expr: if_feature_term (SEP OR SEP if_feature_term)*;
if_feature_term: if_feature_factor (SEP AND SEP if_feature_term)*;
if_feature_factor: NOT SEP if_feature_factor
                 | LP SEP? if_feature_expr SEP? RP
                 | identifier_ref_arg;

identifier_ref_arg : (IDENTIFIER COLON)? IDENTIFIER;
