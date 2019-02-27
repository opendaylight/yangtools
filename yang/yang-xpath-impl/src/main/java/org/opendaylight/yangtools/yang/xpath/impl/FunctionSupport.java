/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangBooleanConstantExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangFunctionCallExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangNumberExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathSupport;

/**
 * This class provides support for validating function call arguments as well as compile-time evaluation.
 */
final class FunctionSupport {
    static final YangFunctionCallExpr POSITION = YangFunctionCallExpr.of(YangFunction.POSITION.getIdentifier());

    private static final YangFunctionCallExpr CURRENT = YangFunctionCallExpr.of(YangFunction.CURRENT.getIdentifier());
    private static final YangFunctionCallExpr LAST = YangFunctionCallExpr.of(YangFunction.LAST.getIdentifier());
    private static final YangFunctionCallExpr LOCAL_NAME = YangFunctionCallExpr.of(
        YangFunction.LOCAL_NAME.getIdentifier());
    private static final YangFunctionCallExpr NAME = YangFunctionCallExpr.of(YangFunction.NAME.getIdentifier());
    private static final YangFunctionCallExpr NAMESPACE_URI = YangFunctionCallExpr.of(
        YangFunction.NAMESPACE_URI.getIdentifier());
    private static final YangFunctionCallExpr NORMALIZE_SPACE = YangFunctionCallExpr.of(
        YangFunction.NORMALIZE_SPACE.getIdentifier());
    private static final YangFunctionCallExpr NUMBER = YangFunctionCallExpr.of(YangFunction.NUMBER.getIdentifier());
    private static final YangFunctionCallExpr STRING = YangFunctionCallExpr.of(YangFunction.STRING.getIdentifier());
    private static final YangFunctionCallExpr STRING_LENGTH = YangFunctionCallExpr.of(
        YangFunction.STRING_LENGTH.getIdentifier());

    private final YangXPathMathSupport mathSupport;
    private final YangNamespaceContext namespaceContext;

    FunctionSupport(final YangNamespaceContext namespaceContext, final YangXPathMathSupport mathSupport) {
        this.namespaceContext = requireNonNull(namespaceContext);
        this.mathSupport = requireNonNull(mathSupport);
    }

    YangExpr functionToExpr(final YangFunction func, final List<YangExpr> args) {
        switch (func) {
            case BIT_IS_SET:
                checkArgument(args.size() == 2, "bit-is-set(node-set, string) takes two arguments");
                break;
            case BOOLEAN:
                return booleanExpr(args);
            case CEILING:
                checkArgument(args.size() == 1, "ceiling(number) takes one argument");
                // TODO: constant folding requires math support
                break;
            case CONCAT:
                return concatExpr(args);
            case CONTAINS:
                return containsExpr(args);
            case COUNT:
                checkArgument(args.size() == 1, "count(node-set) takes one argument");
                // TODO: constant folding requires math support
                break;
            case CURRENT:
                checkArgument(args.isEmpty(), "current() does not take any arguments");
                return CURRENT;
            case DEREF:
                checkArgument(args.size() == 1, "deref(node-set) takes one argument");
                break;
            case DERIVED_FROM:
                return derivedFromExpr(args);
            case DERIVED_FROM_OR_SELF:
                return derivedFromOrSelfExpr(args);
            case ENUM_VALUE:
                checkArgument(args.size() == 1, "enum-value(node-set) takes one argument");
                break;
            case FALSE:
                checkArgument(args.isEmpty(), "false() does not take any arguments");
                return YangBooleanConstantExpr.FALSE;
            case FLOOR:
                checkArgument(args.size() == 1, "floor(number) takes one argument");
                // TODO: constant folding requires math support
                break;
            case ID:
                checkArgument(args.size() == 1, "id(object) takes one argument");
                break;
            case LANG:
                checkArgument(args.size() == 1, "lang(string) takes one argument");
                break;
            case LAST:
                checkArgument(args.isEmpty(), "last() does not take any arguments");
                return LAST;
            case LOCAL_NAME:
                checkArgument(args.size() <= 1, "local-name(node-set?) takes at most one argument");
                if (args.isEmpty()) {
                    return LOCAL_NAME;
                }
                break;
            case NAME:
                checkArgument(args.size() <= 1, "name(node-set?) takes at most one argument");
                if (args.isEmpty()) {
                    return NAME;
                }
                break;
            case NAMESPACE_URI:
                checkArgument(args.size() <= 1, "namespace-uri(node-set?) takes at most one argument");
                if (args.isEmpty()) {
                    return NAMESPACE_URI;
                }
                break;
            case NORMALIZE_SPACE:
                return normalizeSpaceExpr(args);
            case NOT:
                return notExpr(args);
            case NUMBER:
                return numberExpr(args);
            case POSITION:
                checkArgument(args.isEmpty(), "position() does not take any arguments");
                return POSITION;
            case RE_MATCH:
                checkArgument(args.size() == 2, "re-match(string, string) takes two arguments");
                // TODO: static analysis requires XSD regex support -- we should validate args[1] and match it to
                //       args[0] if that is a literal
                break;
            case ROUND:
                checkArgument(args.size() == 1, "round(number) takes one argument");
                // TODO: constant folding requires math support
                break;
            case STARTS_WITH:
                return startsWithExpr(args);
            case STRING:
                return stringExpr(args);
            case STRING_LENGTH:
                return stringLengthExpr(args);
            case SUBSTRING:
                return substringExpr(args);
            case SUBSTRING_AFTER:
                return substringAfterExpr(args);
            case SUBSTRING_BEFORE:
                return substringBeforeExpr(args);
            case SUM:
                checkArgument(args.size() == 1, "sub(node-set) takes one argument");
                // TODO: constant folding requires math support
                break;
            case TRANSLATE:
                checkArgument(args.size() == 3, "translate(string, string, string) takes three arguments");
                // TODO: constant folding?
                break;
            case TRUE:
                checkArgument(args.isEmpty(), "true() does not take any arguments");
                return YangBooleanConstantExpr.TRUE;
            default:
                throw new IllegalStateException("Unhandled function " + func);
        }

        return YangFunctionCallExpr.of(func.getIdentifier(), args);
    }

    private static YangExpr booleanExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 1, "boolean(object) takes one argument");
        final YangExpr arg = args.get(0);
        if (arg instanceof YangBooleanConstantExpr) {
            return arg;
        }
        if (arg instanceof YangLiteralExpr) {
            return YangBooleanConstantExpr.of(!((YangLiteralExpr) arg).getLiteral().isEmpty());
        }

        // TODO: handling YangNumberExpr requires math support

        return YangFunctionCallExpr.of(YangFunction.BOOLEAN.getIdentifier(), args);
    }

    private static YangExpr concatExpr(final List<YangExpr> args) {
        checkArgument(args.size() >= 2, "concat(string, string, string*) takes at least two arguments");

        // TODO: constant folding

        return YangFunctionCallExpr.of(YangFunction.CONCAT.getIdentifier(), args);
    }

    private static YangExpr containsExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "contains(string, string) takes two arguments");
        final YangExpr first = args.get(0);
        if (first instanceof YangLiteralExpr) {
            final YangExpr second = args.get(1);
            if (second instanceof YangLiteralExpr) {
                return YangBooleanConstantExpr.of(
                    ((YangLiteralExpr) first).getLiteral().contains(((YangLiteralExpr) second).getLiteral()));
            }
        }

        // TODO: handling YangNumberExpr requires math support
        return YangFunctionCallExpr.of(YangFunction.CONTAINS.getIdentifier(), args);
    }

    private static YangExpr derivedFromExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "derived-from(node-set, string) takes two arguments");
        // FIXME: coerce second arg to a QName
        return YangFunctionCallExpr.of(YangFunction.DERIVED_FROM.getIdentifier(), args);
    }

    private static YangExpr derivedFromOrSelfExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "derived-from-or-self(node-set, string) takes two arguments");
        // FIXME: coerce second arg to a QName
        return YangFunctionCallExpr.of(YangFunction.DERIVED_FROM_OR_SELF.getIdentifier(), args);
    }

    private static YangExpr notExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 1, "not(boolean) takes one argument");
        final YangExpr arg = args.get(0);
        if (arg instanceof YangBooleanConstantExpr) {
            return YangBooleanConstantExpr.of(((YangBooleanConstantExpr) arg).getValue());
        }

        return YangFunctionCallExpr.of(YangFunction.NOT.getIdentifier(), args);
    }

    private static YangExpr normalizeSpaceExpr(final List<YangExpr> args) {
        checkArgument(args.size() <= 1, "normalize-space(object?) takes at most one argument");
        if (args.isEmpty()) {
            return NORMALIZE_SPACE;
        }
        final YangExpr arg = args.get(0);
        if (arg instanceof YangLiteralExpr) {
            // TODO: normalize value
        }

        return YangFunctionCallExpr.of(YangFunction.NORMALIZE_SPACE.getIdentifier(), args);
    }

    private YangExpr numberExpr(final List<YangExpr> args) {
        checkArgument(args.size() <= 1, "number(object?) takes at most one argument");
        if (args.isEmpty()) {
            return NUMBER;
        }

        final YangExpr arg = args.get(0);
        if (arg instanceof YangNumberExpr) {
            return arg;
        }
        if (arg instanceof YangLiteralExpr) {
            return mathSupport.createNumber(((YangLiteralExpr) arg).getLiteral());
        }
        if (arg instanceof YangBooleanConstantExpr) {
            final boolean value = ((YangBooleanConstantExpr) arg).getValue();
            return mathSupport.createNumber(value ? 1 : 0);
        }

        return YangFunctionCallExpr.of(YangFunction.NUMBER.getIdentifier(), args);
    }

    private static YangExpr startsWithExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "starts-with(string, string) takes two arguments");

        // TODO: constant folding

        return YangFunctionCallExpr.of(YangFunction.STARTS_WITH.getIdentifier(), args);
    }

    private static YangExpr substringBeforeExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "substring-before(string, string) takes two arguments");

        // TODO: constant folding

        return YangFunctionCallExpr.of(YangFunction.SUBSTRING_BEFORE.getIdentifier(), args);
    }

    private static YangExpr substringAfterExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "substring-after(string, string) takes two arguments");

        // TODO: constant folding

        return YangFunctionCallExpr.of(YangFunction.SUBSTRING_AFTER.getIdentifier(), args);
    }

    private static YangExpr substringExpr(final List<YangExpr> args) {
        final int size = args.size();
        checkArgument(size == 2 || size == 3, "substring-(string, number, number?) takes two or three arguments");

        // TODO: constant folding

        return YangFunctionCallExpr.of(YangFunction.SUBSTRING.getIdentifier(), args);
    }

    private static YangExpr stringExpr(final List<YangExpr> args) {
        checkArgument(args.size() <= 1, "string(object?) takes at most one argument");
        if (args.isEmpty()) {
            return STRING;
        }

        final YangExpr arg = args.get(0);
        if (arg instanceof YangLiteralExpr) {
            return arg;
        }
        if (arg instanceof YangBooleanConstantExpr) {
            return ((YangBooleanConstantExpr) arg).asStringLiteral();
        }

        // TODO: handling YangNumberExpr requires math support
        return YangFunctionCallExpr.of(YangFunction.STRING.getIdentifier(), args);
    }

    private YangExpr stringLengthExpr(final List<YangExpr> args) {
        checkArgument(args.size() <= 1, "string-length(object?) takes at most one argument");
        if (args.isEmpty()) {
            return STRING_LENGTH;
        }

        YangExpr first = args.get(0);
        if (first instanceof YangBooleanConstantExpr) {
            first = ((YangBooleanConstantExpr) first).asStringLiteral();
        }
        if (first instanceof YangLiteralExpr) {
            return mathSupport.createNumber(((YangLiteralExpr) first).getLiteral().length());
        }

        return YangFunctionCallExpr.of(YangFunction.STRING_LENGTH.getIdentifier(), args);
    }
}
