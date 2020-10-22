/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

enum Operator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_THAN_EQUALS,
    LESS_THAN,
    LESS_THAN_EQUALS,
    MINUS,
    PLUS,
    AND,
    OR,
    DIV,
    MOD,
    MULTIPLY,
    UNION;

    static Operator forString(final String str) {
        switch (str) {
            case "=":
                return Operator.EQUALS;
            case "!=":
                return Operator.NOT_EQUALS;
            case "-":
                return Operator.MINUS;
            case "+":
                return Operator.PLUS;
            case "and":
                return Operator.AND;
            case "or":
                return Operator.OR;
            case "div":
                return Operator.DIV;
            case "mod":
                return Operator.MOD;
            case "*":
                return Operator.MULTIPLY;
            case ">=":
                return Operator.GREATER_THAN_EQUALS;
            case ">":
                return Operator.GREATER_THAN;
            case "<=":
                return Operator.LESS_THAN_EQUALS;
            case "<":
                return Operator.LESS_THAN;
            case "|":
                return Operator.UNION;
            default:
                throw new IllegalArgumentException("Unknown operator " + str);

        }
    }
}
