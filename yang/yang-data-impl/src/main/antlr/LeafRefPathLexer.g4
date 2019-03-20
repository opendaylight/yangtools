lexer grammar LeafRefPathLexer;

@header {
package org.opendaylight.yangtools.yang.data.impl.leafref;
}

COLON : ':' ;
SLASH : '/' ;
DOTS : '..' ;
EQUAL : '=' ;
LEFT_SQUARE_BRACKET : '[' ;
RIGHT_SQUARE_BRACKET : ']' ;
LEFT_PARENTHESIS : '(' ;
RIGHT_PARENTHESIS : ')' ;

CURRENT_KEYWORD : 'current';

SEP: [ \n\r\t]+ ;
IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_\-.]*;

