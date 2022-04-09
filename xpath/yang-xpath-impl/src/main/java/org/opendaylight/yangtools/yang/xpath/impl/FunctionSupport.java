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

    FunctionSupport(final YangXPathMathSupport mathSupport) {
        this.mathSupport = requireNonNull(mathSupport);
    }

    YangExpr functionToExpr(final YangFunction func, final List<YangExpr> args) {
        return switch (func) {
            case BIT_IS_SET -> {
                checkArgument(args.size() == 2, "bit-is-set(node-set, string) takes two arguments");
                yield call(func, args);
            }
            case BOOLEAN -> booleanExpr(args);
            case CEILING -> {
                checkArgument(args.size() == 1, "ceiling(number) takes one argument");
                // TODO: constant folding requires math support
                yield call(func, args);
            }
            case CONCAT -> concatExpr(args);
            case CONTAINS -> containsExpr(args);
            case COUNT -> {
                checkArgument(args.size() == 1, "count(node-set) takes one argument");
                // TODO: constant folding requires math support
                yield call(func, args);
            }
            case CURRENT -> {
                checkArgument(args.isEmpty(), "current() does not take any arguments");
                yield CURRENT;
            }
            case DEREF -> {
                checkArgument(args.size() == 1, "deref(node-set) takes one argument");
                yield call(func, args);
            }
            case DERIVED_FROM ->  derivedFromExpr(args);
            case DERIVED_FROM_OR_SELF -> derivedFromOrSelfExpr(args);
            case ENUM_VALUE -> {
                checkArgument(args.size() == 1, "enum-value(node-set) takes one argument");
                yield call(func, args);
            }
            case FALSE -> {
                checkArgument(args.isEmpty(), "false() does not take any arguments");
                yield YangBooleanConstantExpr.FALSE;
            }
            case FLOOR -> {
                checkArgument(args.size() == 1, "floor(number) takes one argument");
                // TODO: constant folding requires math support
                yield call(func, args);
            }
            case ID -> {
                checkArgument(args.size() == 1, "id(object) takes one argument");
                yield call(func, args);
            }
            case LANG -> {
                checkArgument(args.size() == 1, "lang(string) takes one argument");
                yield call(func, args);
            }
            case LAST -> {
                checkArgument(args.isEmpty(), "last() does not take any arguments");
                yield LAST;
            }
            case LOCAL_NAME -> {
                checkArgument(args.size() <= 1, "local-name(node-set?) takes at most one argument");
                yield args.isEmpty() ? LOCAL_NAME : call(func, args);
            }
            case NAME -> {
                checkArgument(args.size() <= 1, "name(node-set?) takes at most one argument");
                yield args.isEmpty() ? NAME : call(func, args);
            }
            case NAMESPACE_URI -> {
                checkArgument(args.size() <= 1, "namespace-uri(node-set?) takes at most one argument");
                yield args.isEmpty() ? NAMESPACE_URI : call(func, args);
            }
            case NORMALIZE_SPACE -> normalizeSpaceExpr(args);
            case NOT -> notExpr(args);
            case NUMBER -> numberExpr(args);
            case POSITION -> {
                checkArgument(args.isEmpty(), "position() does not take any arguments");
                yield POSITION;
            }
            case RE_MATCH -> {
                checkArgument(args.size() == 2, "re-match(string, string) takes two arguments");
                // TODO: static analysis requires XSD regex support -- we should validate args[1] and match it to
                //       args[0] if that is a literal
                yield call(func, args);
            }
            case ROUND -> {
                checkArgument(args.size() == 1, "round(number) takes one argument");
                // TODO: constant folding requires math support
                yield call(func, args);
            }
            case STARTS_WITH -> startsWithExpr(args);
            case STRING -> stringExpr(args);
            case STRING_LENGTH -> stringLengthExpr(args);
            case SUBSTRING -> substringExpr(args);
            case SUBSTRING_AFTER -> substringAfterExpr(args);
            case SUBSTRING_BEFORE -> substringBeforeExpr(args);
            case SUM -> {
                checkArgument(args.size() == 1, "sub(node-set) takes one argument");
                // TODO: constant folding requires math support
                yield call(func, args);
            }
            case TRANSLATE -> {
                checkArgument(args.size() == 3, "translate(string, string, string) takes three arguments");
                // TODO: constant folding?
                yield call(func, args);
            }
            case TRUE -> {
                checkArgument(args.isEmpty(), "true() does not take any arguments");
                yield YangBooleanConstantExpr.TRUE;
            }
        };
    }

    private static YangFunctionCallExpr call(final YangFunction func, final List<YangExpr> args) {
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

        return call(YangFunction.BOOLEAN, args);
    }

    private static YangExpr concatExpr(final List<YangExpr> args) {
        checkArgument(args.size() >= 2, "concat(string, string, string*) takes at least two arguments");

        // TODO: constant folding

        return call(YangFunction.CONCAT, args);
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
        return call(YangFunction.CONTAINS, args);
    }

    private static YangExpr derivedFromExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "derived-from(node-set, string) takes two arguments");
        // FIXME: coerce second arg to a QName
        return call(YangFunction.DERIVED_FROM, args);
    }

    private static YangExpr derivedFromOrSelfExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "derived-from-or-self(node-set, string) takes two arguments");
        // FIXME: coerce second arg to a QName
        return call(YangFunction.DERIVED_FROM_OR_SELF, args);
    }

    private static YangExpr notExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 1, "not(boolean) takes one argument");
        final YangExpr arg = args.get(0);
        if (arg instanceof YangBooleanConstantExpr) {
            return YangBooleanConstantExpr.of(((YangBooleanConstantExpr) arg).getValue());
        }

        return call(YangFunction.NOT, args);
    }

    private static YangExpr normalizeSpaceExpr(final List<YangExpr> args) {
        checkArgument(args.size() <= 1, "normalize-space(object?) takes at most one argument");
        if (args.isEmpty()) {
            return NORMALIZE_SPACE;
        }
        // final YangExpr arg = args.get(0);
        // if (arg instanceof YangLiteralExpr) {
        //     // TODO: normalize value
        // }

        return call(YangFunction.NORMALIZE_SPACE, args);
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

        return call(YangFunction.NUMBER, args);
    }

    private static YangExpr startsWithExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "starts-with(string, string) takes two arguments");

        // TODO: constant folding

        return call(YangFunction.STARTS_WITH, args);
    }

    private static YangExpr substringBeforeExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "substring-before(string, string) takes two arguments");

        // TODO: constant folding

        return call(YangFunction.SUBSTRING_BEFORE, args);
    }

    private static YangExpr substringAfterExpr(final List<YangExpr> args) {
        checkArgument(args.size() == 2, "substring-after(string, string) takes two arguments");

        // TODO: constant folding

        return call(YangFunction.SUBSTRING_AFTER, args);
    }

    private static YangExpr substringExpr(final List<YangExpr> args) {
        final int size = args.size();
        checkArgument(size == 2 || size == 3, "substring-(string, number, number?) takes two or three arguments");

        // TODO: constant folding

        return call(YangFunction.SUBSTRING, args);
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
        return call(YangFunction.STRING, args);
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

        return call(YangFunction.STRING_LENGTH, args);
    }
}
