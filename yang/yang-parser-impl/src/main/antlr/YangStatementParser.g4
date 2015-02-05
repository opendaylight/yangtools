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
    tokenVocab=YangStatementLexer;
}

statement : SEP? any_keyword SEP? (argument)? SEP? (SEMICOLON | LEFT_BRACE (statement)* SEP? RIGHT_BRACE SEP?);
any_keyword : YIN_ELEMENT_KEYWORD
            | YANG_VERSION_KEYWORD
            | WHEN_KEYWORD
            | VALUE_KEYWORD
            | USES_KEYWORD
            | UNITS_KEYWORD
            | UNIQUE_KEYWORD
            | TYPEDEF_KEYWORD
            | TYPE_KEYWORD
            | SUBMODULE_KEYWORD
            | STATUS_KEYWORD
            | RPC_KEYWORD
            | REVISION_DATE_KEYWORD
            | REVISION_KEYWORD
            | REQUIRE_INSTANCE_KEYWORD
            | REFINE_KEYWORD
            | REFERENCE_KEYWORD
            | RANGE_KEYWORD
            | PRESENCE_KEYWORD
            | PREFIX_KEYWORD
            | POSITION_KEYWORD
            | PATTERN_KEYWORD
            | PATH_KEYWORD
            | OUTPUT_KEYWORD
            | ORGANIZATION_KEYWORD
            | ORDERED_BY_KEYWORD
            | NOTIFICATION_KEYWORD
            | NAMESPACE_KEYWORD
            | MUST_KEYWORD
            | MODULE_KEYWORD
            | MIN_ELEMENTS_KEYWORD
            | MAX_ELEMENTS_KEYWORD
            | MANDATORY_KEYWORD
            | LIST_KEYWORD
            | LENGTH_KEYWORD
            | LEAF_LIST_KEYWORD
            | LEAF_KEYWORD
            | KEY_KEYWORD
            | INPUT_KEYWORD
            | INCLUDE_KEYWORD
            | IMPORT_KEYWORD
            | IF_FEATURE_KEYWORD
            | IDENTITY_KEYWORD
            | GROUPING_KEYWORD
            | FRACTION_DIGITS_KEYWORD
            | FEATURE_KEYWORD
            | DEVIATE_KEYWORD
            | DEVIATION_KEYWORD
            | EXTENSION_KEYWORD
            | ERROR_MESSAGE_KEYWORD
            | ERROR_APP_TAG_KEYWORD
            | ENUM_KEYWORD
            | DESCRIPTION_KEYWORD
            | DEFAULT_KEYWORD
            | CONTAINER_KEYWORD
            | CONTACT_KEYWORD
            | CONFIG_KEYWORD
            | CHOICE_KEYWORD
            | CASE_KEYWORD
            | BIT_KEYWORD
            | BELONGS_TO_KEYWORD
            | BASE_KEYWORD
            | AUGMENT_KEYWORD
            | ARGUMENT_KEYWORD
            | ANYXML_KEYWORD
            | CURRENT_KEYWORD
            | MIN_KEYWORD
            | MAX_KEYWORD
            | UNBOUNDED_KEYWORD;

argument : STRING | IDENTIFIER;