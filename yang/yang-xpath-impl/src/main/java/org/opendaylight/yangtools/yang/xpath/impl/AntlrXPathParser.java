/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.getChild;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.illegalShape;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.verifyAtLeastChildren;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.verifyChildCount;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.verifyTerminal;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.verifyToken;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.verifyTree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.xpath.XPathExpressionException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangBooleanConstantExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangFilterExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangFunctionCallExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.AxisStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.opendaylight.yangtools.yang.xpath.api.YangNaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangNaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangNegateExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangNumberExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangVariableReferenceExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathSupport;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathNodeType;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.AbbreviatedStepContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.AbsoluteLocationPathNorootContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.AdditiveExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.AndExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.AxisSpecifierContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.EqualityExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.ExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.FilterExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.FunctionCallContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.FunctionNameContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.LocationPathContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.MultiplicativeExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.NCNameContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.NameTestContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.NodeTestContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.OrExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.PathExprNoRootContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.PredicateContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.PrimaryExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.QNameContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.RelationalExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.RelativeLocationPathContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.StepContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.UnaryExprNoRootContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.UnionExprNoRootContext;
import org.opendaylight.yangtools.yang.xpath.impl.xpathParser.VariableReferenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ANTLR-based XPath parser. Uses {@code xpath.g4} ANTLR grammar.
 *
 * @author Robert Varga
 */
final class AntlrXPathParser implements YangXPathParser {
    private static final Logger LOG = LoggerFactory.getLogger(AntlrXPathParser.class);
    private static final Map<String, YangBinaryOperator> BINARY_OPERATORS = Maps.uniqueIndex(
        Arrays.asList(YangBinaryOperator.values()), YangBinaryOperator::toString);
    private static final Map<String, YangXPathNodeType> NODE_TYPES = Maps.uniqueIndex(Arrays.asList(
        YangXPathNodeType.values()), YangXPathNodeType::toString);
    private static final Map<String, YangXPathAxis> XPATH_AXES = Maps.uniqueIndex(Arrays.asList(YangXPathAxis.values()),
        YangXPathAxis::toString);
    private static final Map<QName, YangFunction> YANG_FUNCTIONS = Maps.uniqueIndex(Arrays.asList(
        YangFunction.values()), YangFunction::getIdentifier);

    // Cached for checks in hot path
    private static final AxisStep SELF_STEP = YangXPathAxis.SELF.asStep();

    private final YangXPathMathMode mathMode;
    private final YangXPathMathSupport<?> mathSupport;
    private final YangNamespaceContext namespaceContext;

    AntlrXPathParser(final YangXPathMathMode mathMode, final YangNamespaceContext namespaceContext) {
        this.mathMode = requireNonNull(mathMode);
        this.mathSupport = mathMode.getSupport();
        this.namespaceContext = requireNonNull(namespaceContext);
    }

    @Override
    public YangXPathExpression parseExpression(final String xpath) throws XPathExpressionException {
        // Create a parser and disconnect it from console error output
        final xpathLexer lexer = new xpathLexer(CharStreams.fromString(xpath));
        final xpathParser parser = new xpathParser(new CommonTokenStream(lexer));

        final List<XPathExpressionException> errors = new ArrayList<>();
        final BaseErrorListener listener = new BaseErrorListener() {
            @Override
            public void syntaxError(final @Nullable Recognizer<?, ?> recognizer, final @Nullable Object offendingSymbol,
                    final int line, final int charPositionInLine, final @Nullable String msg,
                    final @Nullable RecognitionException cause) {
                final XPathExpressionException ex = new XPathExpressionException(msg);
                ex.initCause(cause);
                if (errors.isEmpty()) {
                    errors.add(ex);
                } else {
                    errors.get(0).addSuppressed(ex);
                }
            }
        };

        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        final YangExpr expr = parseExpr(parser.main().expr());
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }

        return new AntlrYangXPathExpression(namespaceContext, mathMode, expr, xpath);
    }

    /**
     * Parse and simplify an XPath expression in {@link ExprContext} representation.
     *
     * @param expr ANTLR ExprContext
     * @return A {@link YangExpr}
     * @throws NullPointerException if {@code expr} is null
     * @throws IllegalArgumentException if {@code expr} references an unbound prefix
     */
    private YangExpr parseExpr(final ExprContext expr) {
        final OrExprContext or = expr.orExpr();
        final int size = or.getChildCount();
        if (size == 1) {
            return parseAnd(getChild(or, AndExprContext.class, 0));
        }
        final List<YangExpr> tmp = new ArrayList<>((size + 1) / 2);
        for (int i = 0; i < size; i += 2) {
            tmp.add(parseAnd(getChild(or, AndExprContext.class, i)));
        }
        return YangNaryOperator.OR.exprWith(tmp);
    }

    private YangExpr parseAdditive(final AdditiveExprContext expr) {
        final Iterator<ParseTree> it = expr.children.iterator();
        final YangExpr first = parseMultiplicative(nextContext(it, MultiplicativeExprContext.class));
        return it.hasNext() ? parseAdditiveExpr(first, it) : first;
    }

    private YangExpr parseAnd(final AndExprContext expr) {
        final int size = expr.getChildCount();
        if (size == 1) {
            return parseEquality(getChild(expr, EqualityExprContext.class, 0));
        }
        final List<YangExpr> tmp = new ArrayList<>((size + 1) / 2);
        for (int i = 0; i < size; i += 2) {
            tmp.add(parseEquality(getChild(expr, EqualityExprContext.class, i)));
        }
        return YangNaryOperator.AND.exprWith(tmp);
    }

    private YangExpr parseEquality(final EqualityExprContext expr) {
        final Iterator<ParseTree> it = expr.children.iterator();
        final YangExpr first = parseRelational(nextContext(it, RelationalExprContext.class));
        return it.hasNext() ? parseEqualityExpr(first, it) : first;
    }

    private YangExpr parseFilter(final FilterExprContext expr) {
        final Iterator<ParseTree> it = expr.children.iterator();
        final YangExpr first = parsePrimary(nextContext(it, PrimaryExprContext.class));
        return it.hasNext() ? YangFilterExpr.of(first, ImmutableList.copyOf(Iterators.transform(it,
            tree ->  parsePredicate(verifyTree(PredicateContext.class, tree)))))
                : first;
    }

    private YangExpr parseFunctionCall(final FunctionCallContext expr) {
        // We are mapping functions to RFC7950 YIN namespace, to keep us consistent with type/statement definitions

        final FunctionNameContext name = getChild(expr, FunctionNameContext.class, 0);
        final QName parsed;
        switch (name.getChildCount()) {
            case 1:
                parsed = QName.create(YangConstants.RFC6020_YIN_MODULE, name.getChild(0).getText());
                break;
            case 3:
                parsed = namespaceContext.createQName(name.getChild(0).getText(), name.getChild(2).getText());
                break;
            default:
                throw illegalShape(name);
        }

        final List<YangExpr> args = ImmutableList.copyOf(Lists.transform(expr.expr(), this::parseExpr));
        final YangFunction func = YANG_FUNCTIONS.get(parsed);
        if (func != null) {
            return Functions.functionToExpr(func, args);
        }

        checkArgument(!YangConstants.RFC6020_YIN_MODULE.equals(parsed.getModule()), "Unknown default function %s",
            parsed);
        return YangFunctionCallExpr.of(parsed, args);
    }

    private YangLocationPath parseLocationPath(final LocationPathContext expr) {
        verifyChildCount(expr, 1);
        final ParseTree first = expr.getChild(0);
        if (first instanceof RelativeLocationPathContext) {
            return parseRelativeLocationPath((RelativeLocationPathContext) first);
        }

        final AbsoluteLocationPathNorootContext abs = verifyTree(AbsoluteLocationPathNorootContext.class, first);
        verifyChildCount(abs, 2);

        final Deque<Step> steps = parseLocationPathSteps(getChild(abs, RelativeLocationPathContext.class, 1));
        parseStepShorthand(abs.getChild(0)).ifPresent(steps::addFirst);

        return YangLocationPath.of(true, steps);
    }

    private YangExpr parseMultiplicative(final MultiplicativeExprContext expr) {
        final ParseTree first = expr.getChild(0);
        final YangExpr left;
        if (first instanceof UnaryExprNoRootContext) {
            left = parseUnary((UnaryExprNoRootContext) first);
        } else {
            left = YangLocationPath.root();
        }
        if (expr.getChildCount() == 1) {
            return left;
        }

        verifyChildCount(expr, 3);
        final YangBinaryOperator operator = parseOperator(expr.getChild(1));
        final YangExpr right = parseMultiplicative(getChild(expr, MultiplicativeExprContext.class, 2));
        final Optional<YangExpr> simple = simplifyNumbers(operator, left, right);
        return simple.isPresent() ? simple.get() : operator.exprWith(left, right);
    }

    private YangExpr parsePathExpr(final PathExprNoRootContext expr) {
        final ParseTree first = expr.getChild(0);
        if (first instanceof LocationPathContext) {
            return parseLocationPath((LocationPathContext) first);
        }

        final YangExpr filter = parseFilter(verifyTree(FilterExprContext.class, first));
        if (expr.getChildCount() == 1) {
            return filter;
        }

        verifyChildCount(expr, 3);

        // FIXME: this actually is a concatenation
        return parseOperator(expr.getChild(1)).exprWith(filter,
            parseRelativeLocationPath(getChild(expr, RelativeLocationPathContext.class, 2)));
    }

    private YangExpr parsePredicate(final PredicateContext expr) {
        verifyChildCount(expr, 3);
        return parseExpr(getChild(expr, ExprContext.class, 1));
    }

    private YangExpr parsePrimary(final PrimaryExprContext expr) {
        if (expr.getChildCount() == 3) {
            return parseExpr(getChild(expr, ExprContext.class, 1));
        }

        verifyChildCount(expr, 1);
        final ParseTree first = expr.getChild(0);
        if (first instanceof TerminalNode) {
            return parseTerminal((TerminalNode) first);
        }
        if (first instanceof FunctionCallContext) {
            return parseFunctionCall((FunctionCallContext) first);
        }
        if (first instanceof VariableReferenceContext) {
            return YangVariableReferenceExpr.of(parseQName(((VariableReferenceContext) first).qName()));
        }
        throw illegalShape(first);
    }

    private YangExpr parseRelational(final RelationalExprContext expr) {
        final Iterator<ParseTree> it = expr.children.iterator();
        final YangExpr first = parseAdditive(nextContext(it, AdditiveExprContext.class));
        return it.hasNext() ? parseRelationalExpr(first, it) : first;
    }

    private Deque<Step> parseLocationPathSteps(final RelativeLocationPathContext expr) {
        final Deque<Step> steps = new ArrayDeque<>(expr.getChildCount());
        final Iterator<ParseTree> it = expr.children.iterator();
        steps.add(parseStep(nextContext(it, StepContext.class)));

        while (it.hasNext()) {
            parseStepShorthand(it.next()).ifPresent(steps::add);

            // Parse step and add it if it's not SELF_STEP
            final Step step = parseStep(nextContext(it, StepContext.class));
            if (!SELF_STEP.equals(step)) {
                steps.add(step);
            }
        }

        return steps;
    }

    private YangLocationPath parseRelativeLocationPath(final RelativeLocationPathContext expr) {
        return YangLocationPath.of(false, parseLocationPathSteps(expr));
    }

    private YangExpr parseTerminal(final TerminalNode term) {
        final String text = term.getText();
        switch (term.getSymbol().getType()) {
            case xpathParser.Literal:
                // We have to strip quotes
                return YangLiteralExpr.of(text.substring(1, text.length() - 1));
            case xpathParser.Number:
                return mathSupport.createNumber(text);
            default:
                throw illegalShape(term);
        }
    }

    private YangExpr parseUnary(final UnaryExprNoRootContext expr) {
        // any number of '-' and an union expr
        final int size = verifyAtLeastChildren(expr, 1);
        final YangExpr ret = parseUnion(getChild(expr, UnionExprNoRootContext.class, size - 1));
        if (size % 2 != 0) {
            // Even number of '-' tokens cancel out
            return ret;
        }

        return ret instanceof YangNumberExpr ? mathSupport.negateNumber((YangNumberExpr<?, ?>) ret)
                : YangNegateExpr.of(ret);
    }

    private YangExpr parseUnion(final UnionExprNoRootContext expr) {
        final ParseTree first = expr.getChild(0);
        final YangExpr path;
        if (first instanceof PathExprNoRootContext) {
            path = parsePathExpr((PathExprNoRootContext) first);
            if (expr.getChildCount() == 1) {
                return path;
            }
        } else {
            path = YangLocationPath.root();
        }

        verifyChildCount(expr, 3);
        final YangExpr union = parseUnion(getChild(expr, UnionExprNoRootContext.class, 2));

        // Deduplicate expressions so we do not perform useless unioning
        final Set<YangExpr> expressions = new LinkedHashSet<>();
        expressions.add(path);
        if (union instanceof YangNaryExpr) {
            // If the result is a union expression, integrate it into this expression
            final YangNaryExpr nary = (YangNaryExpr) union;
            if (nary.getOperator() == YangNaryOperator.UNION) {
                expressions.addAll(nary.getExpressions());
            } else {
                expressions.add(union);
            }
        } else {
            expressions.add(union);
        }

        return YangNaryOperator.UNION.exprWith(expressions);
    }

    private YangExpr parseAdditiveExpr(final YangExpr left, final Iterator<ParseTree> it) {
        YangExpr ret = left;
        do {
            final YangBinaryOperator operator = nextOperator(it);
            final YangExpr right = parseMultiplicative(nextContext(it, MultiplicativeExprContext.class));
            final Optional<YangExpr> simple = simplifyNumbers(operator, ret, right);
            ret = simple.isPresent() ? simple.get() : operator.exprWith(ret, right);
        } while (it.hasNext());

        return ret;
    }

    private Optional<YangExpr> simplifyNumbers(final YangBinaryOperator operator, final YangExpr left,
            final YangExpr right) {
        if (left instanceof YangNumberExpr && right instanceof YangNumberExpr) {
            // Constant folding on numbers -- precision plays a role here
            return mathSupport.tryEvaluate(operator, (YangNumberExpr<?, ?>)left, (YangNumberExpr<?, ?>)right);
        }
        return Optional.empty();
    }

    private YangExpr parseEqualityExpr(final YangExpr left, final Iterator<ParseTree> it) {
        YangExpr ret = left;
        do {
            final YangBinaryOperator operator = nextOperator(it);
            final YangExpr right = parseRelational(nextContext(it, RelationalExprContext.class));

            if (left.equals(right)) {
                // Constant folding on expression level: equal expressions are result in equal results
                switch (operator) {
                    case EQUALS:
                        return YangBooleanConstantExpr.TRUE;
                    case NOT_EQUALS:
                        return YangBooleanConstantExpr.FALSE;
                    default:
                        break;
                }
            }

            final Optional<YangExpr> simple = simplifyNumbers(operator, ret, right);
            ret = simple.isPresent() ? simple.get() : operator.exprWith(ret, right);
        } while (it.hasNext());

        return ret;
    }

    private YangExpr parseRelationalExpr(final YangExpr left, final Iterator<ParseTree> it) {
        YangExpr ret = left;
        do {
            final YangBinaryOperator operator = nextOperator(it);
            final YangExpr right = parseAdditive(nextContext(it, AdditiveExprContext.class));
            final Optional<YangExpr> simple = simplifyNumbers(operator, ret, right);
            ret = simple.isPresent() ? simple.get() : nextOperator(it).exprWith(ret, right);
        } while (it.hasNext());

        return ret;
    }

    private QName parseQName(final QNameContext expr) {
        switch (expr.getChildCount()) {
            case 1:
                return namespaceContext.createQName(getChild(expr, NCNameContext.class, 0).getText());
            case 3:
                return namespaceContext.createQName(getChild(expr, NCNameContext.class, 0).getText(),
                    getChild(expr, NCNameContext.class, 2).getText());
            default:
                throw illegalShape(expr);
        }
    }

    private Step parseStep(final StepContext expr) {
        if (expr.getChildCount() == 1) {
            final AbbreviatedStepContext abbrev = getChild(expr, AbbreviatedStepContext.class, 0);
            verifyChildCount(abbrev, 1);
            switch (getTerminalType(abbrev, 0)) {
                case xpathParser.DOT:
                    return YangXPathAxis.SELF.asStep();
                case xpathParser.DOTDOT:
                    return YangXPathAxis.PARENT.asStep();
                default:
                    throw illegalShape(abbrev);
            }
        }

        final int size = verifyAtLeastChildren(expr, 2);
        final List<YangExpr> predicates = new ArrayList<>(size - 2);
        for (int i = 2; i < size; ++i) {
            predicates.add(parsePredicate(getChild(expr, PredicateContext.class, i)));
        }

        final YangXPathAxis axis = parseAxis(getChild(expr, AxisSpecifierContext.class, 0));
        final NodeTestContext nodeTest = getChild(expr, NodeTestContext.class, 1);
        switch (nodeTest.getChildCount()) {
            case 1:
                final NameTestContext nameChild = getChild(nodeTest, NameTestContext.class, 0);
                final ParseTree first = nameChild.getChild(0);
                if (first instanceof TerminalNode) {
                    verify(((TerminalNode) first).getSymbol().getType() == xpathParser.MUL);
                    return axis.asStep(predicates);
                }
                return axis.asStep(parseQName(verifyTree(QNameContext.class, first)), predicates);
            case 3:
                return axis.asStep(parseNodeType(nodeTest.getChild(0)), predicates);
            case 4:
                final String text = verifyToken(nodeTest, 2, xpathParser.Literal).getText();
                return axis.asStep(text.substring(1, text.length() - 1), predicates);
            default:
                throw illegalShape(nodeTest);
        }
    }

    private static YangXPathAxis parseAxis(final AxisSpecifierContext expr) {
        switch (expr.getChildCount()) {
            case 0:
                return YangXPathAxis.CHILD;
            case 1:
                verify(getTerminalType(expr, 0) == xpathParser.AT, "Unhandled axis specifier shape %s", expr);
                return YangXPathAxis.ATTRIBUTE;
            case 2:
                final String str = verifyTerminal(expr.getChild(0)).getText();
                return verifyNotNull(XPATH_AXES.get(str), "Unhandled axis %s", str);
            default:
                throw illegalShape(expr);
        }
    }

    private static <T extends ParserRuleContext> T nextContext(final Iterator<ParseTree> it, final Class<T> type) {
        return verifyTree(type, it.next());
    }

    private static YangBinaryOperator nextOperator(final Iterator<ParseTree> it) {
        return parseOperator(it.next());
    }

    private static int getTerminalType(final ParseTree parent, final int offset) {
        return verifyTerminal(parent.getChild(offset)).getSymbol().getType();
    }

    private static YangXPathNodeType parseNodeType(final ParseTree tree) {
        final String str = verifyTerminal(tree).getText();
        return verifyNotNull(NODE_TYPES.get(str), "Unhandled node type %s", str);
    }

    private static YangBinaryOperator parseOperator(final ParseTree tree) {
        final String str = verifyTerminal(tree).getText();
        return verifyNotNull(BINARY_OPERATORS.get(str), "Unhandled operator %s", str);
    }

    private static Optional<Step> parseStepShorthand(final ParseTree tree) {
        switch (verifyTerminal(tree).getSymbol().getType()) {
            case xpathParser.PATHSEP:
                return Optional.empty();
            case xpathParser.ABRPATH:
                return Optional.of(YangXPathAxis.DESCENDANT_OR_SELF.asStep());
            default:
                throw illegalShape(tree);
        }
    }
}
