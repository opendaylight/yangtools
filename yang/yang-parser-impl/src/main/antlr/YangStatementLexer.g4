//
// Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
lexer grammar YangStatementLexer;

@header {
package org.opendaylight.yangtools.antlrv4.code.gen;
}

tokens{
    SEMICOLON,
    LEFT_BRACE,
    RIGHT_BRACE,
    SEP,
    IDENTIFIER
}

SEMICOLON : ';' ->type(SEMICOLON);
LEFT_BRACE : '{' ->type(LEFT_BRACE);
RIGHT_BRACE : '}' ->type(RIGHT_BRACE);

YIN_ELEMENT_KEYWORD : 'yin-element';
YANG_VERSION_KEYWORD: 'yang-version';
WHEN_KEYWORD : 'when';
VALUE_KEYWORD : 'value';
USES_KEYWORD : 'uses';
UNITS_KEYWORD : 'units';
UNIQUE_KEYWORD : 'unique';
TYPEDEF_KEYWORD : 'typedef';
TYPE_KEYWORD : 'type';
SUBMODULE_KEYWORD : 'submodule';
STATUS_KEYWORD : 'status';
RPC_KEYWORD : 'rpc';
REVISION_DATE_KEYWORD : 'revision-date';
REVISION_KEYWORD : 'revision';
REQUIRE_INSTANCE_KEYWORD : 'require-instance';
REFINE_KEYWORD : 'refine';
REFERENCE_KEYWORD : 'reference';
RANGE_KEYWORD : 'range';
PRESENCE_KEYWORD : 'presence';
PREFIX_KEYWORD : 'prefix';
POSITION_KEYWORD : 'position';
PATTERN_KEYWORD : 'pattern';
PATH_KEYWORD : 'path';
OUTPUT_KEYWORD : 'output';
ORGANIZATION_KEYWORD: 'organization';
ORDERED_BY_KEYWORD : 'ordered-by';
NOTIFICATION_KEYWORD: 'notification';
NAMESPACE_KEYWORD : 'namespace';
MUST_KEYWORD : 'must';
MODULE_KEYWORD : 'module';
MIN_ELEMENTS_KEYWORD : 'min-elements';
MAX_ELEMENTS_KEYWORD : 'max-elements';
MANDATORY_KEYWORD : 'mandatory';
LIST_KEYWORD : 'list';
LENGTH_KEYWORD : 'length';
LEAF_LIST_KEYWORD : 'leaf-list';
LEAF_KEYWORD : 'leaf';
KEY_KEYWORD : 'key';
INPUT_KEYWORD : 'input';
INCLUDE_KEYWORD : 'include';
IMPORT_KEYWORD : 'import';
IF_FEATURE_KEYWORD : 'if-feature';
IDENTITY_KEYWORD : 'identity';
GROUPING_KEYWORD : 'grouping';
FRACTION_DIGITS_KEYWORD : 'fraction-digits';
FEATURE_KEYWORD : 'feature';
DEVIATE_KEYWORD : 'deviate';
DEVIATION_KEYWORD : 'deviation';
EXTENSION_KEYWORD : 'extension';
ERROR_MESSAGE_KEYWORD : 'error-message';
ERROR_APP_TAG_KEYWORD : 'error-app-tag';
ENUM_KEYWORD : 'enum';
DESCRIPTION_KEYWORD : 'description';
DEFAULT_KEYWORD : 'default';
CONTAINER_KEYWORD : 'container';
CONTACT_KEYWORD : 'contact';
CONFIG_KEYWORD : 'config';
CHOICE_KEYWORD: 'choice';
CASE_KEYWORD : 'case';
BIT_KEYWORD : 'bit';
BELONGS_TO_KEYWORD : 'belongs-to';
BASE_KEYWORD : 'base';
AUGMENT_KEYWORD : 'augment';
ARGUMENT_KEYWORD : 'argument';
ANYXML_KEYWORD : 'anyxml';
CURRENT_KEYWORD : 'current';
MIN_KEYWORD : 'min';
MAX_KEYWORD : 'max';
UNBOUNDED_KEYWORD : 'unbounded';

SEP: [ \n\r\t]+ -> type(SEP);
IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_\-.]* -> type(IDENTIFIER);

fragment SUB_STRING : ('"' (ESC | ~["])*'"') | ('\'' (ESC | ~['])*'\'');
fragment ESC : '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE : 'u' HEX HEX HEX HEX;
fragment HEX : [0-9a-fA-F] ;

STRING : ((~( '\r' | '\n' | '\t' | ' ' | ';' | '{' | '"' | '\'' | ':' | '/' | '=' | '[' | ']' )~( '\r' | '\n' | '\t' | ' ' | ';' | '{' | ':' | '/' | '=' | '[' | ']')* ) | SUB_STRING );