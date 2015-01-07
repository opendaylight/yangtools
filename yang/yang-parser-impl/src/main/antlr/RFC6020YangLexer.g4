//
// Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
lexer grammar RFC6020YangLexer;

@header {
package org.opendaylight.yangtools.antlrv4.code.gen;
}

tokens{
    SEMICOLON,
    LEFT_BRACE,
    RIGHT_BRACE,
    COLON,
    SLASH,
    DOTS,
    EQUAL,
    LEFT_SQUARE_BRACKET,
    RIGHT_SQUARE_BRACKET,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS,
    IDENTIFIER,
    SEP
}


PLUS : '+'-> pushMode(VALUE_MODE);
//WS : [ \n\r\t] -> skip;
LINE_COMMENT :  [ \n\r\t]* ('//' (~[\r\n]*)) [ \n\r\t]* -> skip;

START_BLOCK_COMMENT : [ \n\r\t]* '/*' ->pushMode(BLOCK_COMMENT_MODE), skip ;


SEMICOLON : ';' ->type(SEMICOLON);
LEFT_BRACE : '{' ->type(LEFT_BRACE);
RIGHT_BRACE : '}' ->type(RIGHT_BRACE);
COLON : ':' -> type(COLON);
SLASH : '/' ->type(SLASH);
DOTS : '..' ->type(DOTS);
EQUAL : '=' ->type(EQUAL);
LEFT_SQUARE_BRACKET : '[' ->type(LEFT_SQUARE_BRACKET);
RIGHT_SQUARE_BRACKET : ']' ->type(RIGHT_SQUARE_BRACKET);
LEFT_PARENTHESIS : '(' ->type(LEFT_PARENTHESIS);
RIGHT_PARENTHESIS : ')' ->type(RIGHT_PARENTHESIS);
BAR : '|';

/**
 * IMPORTANT: If introducing new literal keyword, please add same keyword to any_keyword rule in RFC6020YangParser.g4.
 *
 * ANTLR will emit keyword token also in places, where YANG ABNF is expecting IDENTFIER. This could be fixed
 * by changing tokenizer modes, but unfortunatelly there is not enough information in token stream to safelly understand
 * if normal or extension statement is started.
 *
 * IMPORTANT: Each keyword which represents YANG statement with argument MUST push VALUE_MODE to parser stack,
 * in order to correctly process YANG multi line strings and string concatenation in argument.
 *
 * Keywords for statements, which do NOT take arguments MUST NOT push VALUE_MODE.
 *
 */
YIN_ELEMENT_KEYWORD : 'yin-element'-> pushMode(VALUE_MODE);
YANG_VERSION_KEYWORD: 'yang-version'-> pushMode(VALUE_MODE);
WHEN_KEYWORD : 'when'-> pushMode(VALUE_MODE);
VALUE_KEYWORD : 'value'-> pushMode(VALUE_MODE);
USES_KEYWORD : 'uses'-> pushMode(VALUE_MODE);
UNITS_KEYWORD : 'units'-> pushMode(VALUE_MODE);
UNIQUE_KEYWORD : 'unique'-> pushMode(VALUE_MODE);
TYPEDEF_KEYWORD : 'typedef'-> pushMode(VALUE_MODE);
TYPE_KEYWORD : 'type'-> pushMode(VALUE_MODE);
SUBMODULE_KEYWORD : 'submodule'-> pushMode(VALUE_MODE);
STATUS_KEYWORD : 'status'-> pushMode(VALUE_MODE);
RPC_KEYWORD : 'rpc'-> pushMode(VALUE_MODE);
REVISION_DATE_KEYWORD : 'revision-date'-> pushMode(VALUE_MODE);
REVISION_KEYWORD : 'revision'-> pushMode(VALUE_MODE);
REQUIRE_INSTANCE_KEYWORD : 'require-instance'-> pushMode(VALUE_MODE);
REFINE_KEYWORD : 'refine'-> pushMode(VALUE_MODE);
REFERENCE_KEYWORD : 'reference'-> pushMode(VALUE_MODE);
RANGE_KEYWORD : 'range'-> pushMode(VALUE_MODE);
PRESENCE_KEYWORD : 'presence'-> pushMode(VALUE_MODE);
PREFIX_KEYWORD : 'prefix'-> pushMode(VALUE_MODE);
POSITION_KEYWORD : 'position'-> pushMode(VALUE_MODE);
PATTERN_KEYWORD : 'pattern'-> pushMode(VALUE_MODE);
PATH_KEYWORD : 'path'-> pushMode(VALUE_MODE);
OUTPUT_KEYWORD : 'output';
ORGANIZATION_KEYWORD: 'organization'-> pushMode(VALUE_MODE);
ORDERED_BY_KEYWORD : 'ordered-by'-> pushMode(VALUE_MODE);
NOTIFICATION_KEYWORD: 'notification'-> pushMode(VALUE_MODE);
NAMESPACE_KEYWORD : 'namespace'-> pushMode(VALUE_MODE);
MUST_KEYWORD : 'must'-> pushMode(VALUE_MODE);
MODULE_KEYWORD : 'module'-> pushMode(VALUE_MODE);
MIN_ELEMENTS_KEYWORD : 'min-elements';
MAX_ELEMENTS_KEYWORD : 'max-elements';
MANDATORY_KEYWORD : 'mandatory'-> pushMode(VALUE_MODE);
LIST_KEYWORD : 'list'-> pushMode(VALUE_MODE);
LENGTH_KEYWORD : 'length'-> pushMode(VALUE_MODE);
LEAF_LIST_KEYWORD : 'leaf-list'-> pushMode(VALUE_MODE);
LEAF_KEYWORD : 'leaf'-> pushMode(VALUE_MODE);
KEY_KEYWORD : 'key'-> pushMode(VALUE_MODE);
INPUT_KEYWORD : 'input';
INCLUDE_KEYWORD : 'include'-> pushMode(VALUE_MODE);
IMPORT_KEYWORD : 'import'-> pushMode(VALUE_MODE);
IF_FEATURE_KEYWORD : 'if-feature'-> pushMode(VALUE_MODE);
IDENTITY_KEYWORD : 'identity'-> pushMode(VALUE_MODE);
GROUPING_KEYWORD : 'grouping'-> pushMode(VALUE_MODE);
FRACTION_DIGITS_KEYWORD : 'fraction-digits'-> pushMode(VALUE_MODE);
FEATURE_KEYWORD : 'feature'-> pushMode(VALUE_MODE);
DEVIATE_KEYWORD : 'deviate'-> pushMode(VALUE_MODE);
DEVIATION_KEYWORD : 'deviation'-> pushMode(VALUE_MODE);
EXTENSION_KEYWORD : 'extension'-> pushMode(VALUE_MODE);
ERROR_MESSAGE_KEYWORD : 'error-message'-> pushMode(VALUE_MODE);
ERROR_APP_TAG_KEYWORD : 'error-app-tag'-> pushMode(VALUE_MODE);
ENUM_KEYWORD : 'enum'-> pushMode(VALUE_MODE);
DESCRIPTION_KEYWORD : 'description'-> pushMode(VALUE_MODE);
DEFAULT_KEYWORD : 'default'-> pushMode(VALUE_MODE);
CONTAINER_KEYWORD : 'container'-> pushMode(VALUE_MODE);
CONTACT_KEYWORD : 'contact'-> pushMode(VALUE_MODE);
CONFIG_KEYWORD : 'config'-> pushMode(VALUE_MODE);
CHOICE_KEYWORD: 'choice'-> pushMode(VALUE_MODE);
CASE_KEYWORD : 'case'-> pushMode(VALUE_MODE);
BIT_KEYWORD : 'bit'-> pushMode(VALUE_MODE);
BELONGS_TO_KEYWORD : 'belongs-to'-> pushMode(VALUE_MODE);
BASE_KEYWORD : 'base'-> pushMode(VALUE_MODE);
AUGMENT_KEYWORD : 'augment'-> pushMode(VALUE_MODE);
ARGUMENT_KEYWORD : 'argument'-> pushMode(VALUE_MODE);
ANYXML_KEYWORD : 'anyxml'-> pushMode(VALUE_MODE);
CURRENT_KEYWORD : 'current';
MIN_KEYWORD : 'min';
MAX_KEYWORD : 'max';
UNBOUNDED_KEYWORD : 'unbounded';

/* RFC6020 says: an identifier MUST NOT start with ((’X’|’x’) (’M’|’m’) (’L’|’l’))
 * Otherwise a range_arg or length_arg (for an example 'min..123') is matched by
 * Lexer as IDENTIFIER token.
 *
 * Below is workaround how to allow an identifier start with 'm', but an identifier MUST NOT start with
 * substrings 'min.' nor 'max.'.
 */

IDENTIFIER : ([a-ln-zA-Z_][a-zA-Z0-9_\-.]*
             | 'm'  | 'm'[b-hj-zA-Z_\-.][a-zA-Z0-9_\-.]*
             | 'ma' | 'ma'[a-wy-zA-Z_\-.][a-zA-Z0-9_\-.]*
             | 'mi' | 'mi'[a-mo-zA-Z_\-.][a-zA-Z0-9_\-.]*
             | 'max'| 'max'[a-zA-Z_\-][a-zA-Z0-9_\-.]*
             | 'min'| 'min'[a-zA-Z_\-][a-zA-Z0-9_\-.]*) -> pushMode(VALUE_MODE);

DATE_ARG : [0-9] [0-9] [0-9] [0-9] '-' [0-9] [0-9] '-' [0-9] [0-9] ;

ZERO : '0';
NEGATIVE_INTEGER_VALUE : '-' (POSITIVE_INTEGER_VALUE | ZERO);
POSITIVE_INTEGER_VALUE : [1-9][0-9]*;
DECIMAL_VALUE : (POSITIVE_INTEGER_VALUE | ZERO | NEGATIVE_INTEGER_VALUE) '.' [0-9]+;

UNQUOTED_ZERO : '"0"';
UNQUOTED_NEGATIVE_INTEGER_VALUE : '"-' (POSITIVE_INTEGER_VALUE | ZERO) '"';
UNQUOTED_POSITIVE_INTEGER_VALUE : '"'[1-9][0-9]*'"';
UNQUOTED_DECIMAL_VALUE : '"'(POSITIVE_INTEGER_VALUE | ZERO | NEGATIVE_INTEGER_VALUE) '.' [0-9]+'"';

SEP: [ \n\r\t]+ -> type(SEP);
DEF_MODE_STRING: ('"' (ESC | ~["])*'"') | ('\'' (ESC | ~['])*'\'');

mode VALUE_MODE;

IDENTIFIER2 : [a-zA-Z_][a-zA-Z0-9_\-.]* ->type(IDENTIFIER);

fragment ESC :  '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;

END_IDENTIFIER_SEMICOLON : ';' -> type(SEMICOLON),popMode;
END_IDENTIFIER_LEFT_BRACE : '{' ->type(LEFT_BRACE), popMode;
END_COLON : ':' ->type(COLON), popMode;
END_SLASH : '/' ->type(SLASH), popMode;
//END_LEFT_PARENTHESIS : '(' ->type(LEFT_PARENTHESIS), popMode;
END_EQUAL : '=' ->type(EQUAL), popMode;
END_LEFT_SQUARE_BRACKET : '[' ->type(LEFT_SQUARE_BRACKET), popMode;
END_RIGHT_SQUARE_BRACKET : ']' ->type(RIGHT_SQUARE_BRACKET), popMode;
//| ':'

fragment SUB_STRING : ('"' (ESC | ~["])*'"') | ('\'' (ESC | ~['])*'\'') ;

START_BLOCK_COMMENT2 : '/*' ->pushMode(BLOCK_COMMENT_MODE), skip ;
STRING: ((~( '\r' | '\n' | '\t' | ' ' | ';' | '{' | '"' | '\'' | ':' | '/' | '=' | '[' | ']' )~( '\r' | '\n' | '\t' | ' ' | ';' | '{' | ':' | '/' | '=' | '[' | ']')* ) | SUB_STRING ) ->popMode;// IDENTIFIER ;

//OPTSEP: []
SEP2: [ \n\r\t]+ -> type(SEP);
//S : [ \n\r\t] -> skip;

mode BLOCK_COMMENT_MODE;
END_BLOCK_COMMENT : '*/' [ \n\r\t]* -> popMode,skip;
BLOCK_COMMENT :  . ->more,skip;