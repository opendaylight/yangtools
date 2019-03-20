parser grammar LeafRefPathParser;

@header {
package org.opendaylight.yangtools.yang.data.impl.leafref;
}

options{
 tokenVocab = LeafRefPathLexer;
}

path_arg : absolute_path | relative_path;

absolute_path : (SLASH node_identifier (path_predicate)*)+;

relative_path : (DOTS SLASH)* descendant_path;

descendant_path : node_identifier ((path_predicate)* absolute_path)?;

path_predicate : LEFT_SQUARE_BRACKET SEP? path_equality_expr SEP? RIGHT_SQUARE_BRACKET;

path_equality_expr : node_identifier SEP? EQUAL SEP? path_key_expr;

path_key_expr : current_function_invocation SEP? SLASH SEP? rel_path_keyexpr;

rel_path_keyexpr : (DOTS SEP? SLASH SEP?)* (node_identifier SEP? SLASH SEP?)* node_identifier;

node_identifier : (prefix COLON)? identifier;

current_function_invocation : CURRENT_KEYWORD SEP? LEFT_PARENTHESIS SEP? RIGHT_PARENTHESIS;

prefix : identifier;

identifier: IDENTIFIER | CURRENT_KEYWORD;

