/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6020.parser;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgLexer;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser.Absolute_pathContext;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser.Deref_exprContext;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser.Deref_function_invocationContext;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser.Descendant_pathContext;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser.Node_identifierContext;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser.Path_key_exprContext;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser.Path_predicateContext;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser.Path_strContext;
import org.opendaylight.yangtools.rfc6020.parser.antlr.PathArgParser.Relative_pathContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangFunctionCallExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Absolute;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.opendaylight.yangtools.yang.xpath.api.YangPathExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;

final class PathArgumentParser {
    private static final YangFunctionCallExpr CURRENT_CALL =
        YangFunctionCallExpr.of(YangFunction.CURRENT.getIdentifier());

    private final StmtContext<?, ?, ?> ctx;

    private PathArgumentParser(final StmtContext<?, ?, ?> ctx) {
        this.ctx = requireNonNull(ctx);
    }

    static PathExpression parseExpression(final StmtContext<?, ?, ?> ctx, final String pathArg) {
        final var pathArgChild = SourceExceptionParser.parseString(PathArgLexer::new, PathArgParser::new,
            PathArgParser::path_arg, ctx.sourceReference(), pathArg).getChild(0);

        return switch (pathArgChild) {
            case Path_strContext str -> new PathExpression.LocationPath(pathArg,
                new PathArgumentParser(ctx).parsePathStr(str));
            case Deref_exprContext deref -> {
                final var parser = new PathArgumentParser(ctx);
                yield new PathExpression.Deref(pathArg,
                    parser.parseRelative(getChild(deref, 0, Deref_function_invocationContext.class)
                        .getChild(Relative_pathContext.class, 0)),
                    parser.parseRelative(getChild(deref, deref.getChildCount() - 1, Relative_pathContext.class)));
            }
            default -> throw new IllegalStateException("Unsupported child " + pathArgChild);
        };
    }

    private YangLocationPath parsePathStr(final Path_strContext path) {
        final var childPath = path.getChild(0);
        return switch (childPath) {
            case Absolute_pathContext absolute -> parseAbsolute(absolute);
            case Relative_pathContext relative -> parseRelative(relative);
            default -> throw new IllegalStateException("Unsupported child " + childPath);
        };
    }

    private Absolute parseAbsolute(final Absolute_pathContext absolute) {
        final var steps = new ArrayList<Step>();
        fillSteps(absolute, steps);
        return YangLocationPath.absolute(steps);
    }

    private Relative parseRelative(final Relative_pathContext relative) {
        final int relativeChildren = relative.getChildCount();
        verify(relativeChildren % 2 != 0, "Unexpected child count %s", relativeChildren);

        final int stepCount = relativeChildren / 2;
        final var steps = new ArrayList<Step>(stepCount);
        for (int i = 0; i < stepCount; ++i) {
            steps.add(YangXPathAxis.PARENT.asStep());
        }
        return parseRelative(relative, steps);
    }

    private Relative parseRelative(final Relative_pathContext relative, final List<Step> steps) {
        final int relativeChildren = relative.getChildCount();
        final var descendant = getChild(relative, relativeChildren - 1, Descendant_pathContext.class);
        final var qname = getChild(descendant, 0, Node_identifierContext.class);
        final int descandantChildren = descendant.getChildCount();
        if (descandantChildren > 1) {
            final var predicates = new ArrayList<YangExpr>(descandantChildren);
            for (int i = 1; i < descandantChildren - 1; ++i) {
                predicates.add(parsePathPredicate(getChild(descendant, i, Path_predicateContext.class)));
            }
            steps.add(createChildStep(qname, predicates));
            fillSteps(getChild(descendant, descandantChildren - 1, Absolute_pathContext.class), steps);
        } else {
            steps.add(createChildStep(qname, ImmutableList.of()));
        }

        return YangLocationPath.relative(steps);
    }

    private void fillSteps(final Absolute_pathContext absolute, final List<Step> output) {
        final var predicates = new ArrayList<YangExpr>();
        var qname = getChild(absolute, 1, Node_identifierContext.class);

        final int children = absolute.getChildCount();
        for (int i = 2; i < children; ++i) {
            switch (absolute.getChild(i)) {
                case Node_identifierContext identifier -> {
                    output.add(createChildStep(qname, predicates));
                    predicates.clear();
                    qname = identifier;
                }
                case Path_predicateContext predicate -> predicates.add(parsePathPredicate(predicate));
                default -> {
                    // No-op
                }
            }
        }

        output.add(createChildStep(qname, predicates));
    }

    private static <T> T getChild(final ParseTree parent, final int offset, final Class<T> clazz) {
        final var child = parent.getChild(offset);
        verify(clazz.isInstance(child), "Unexpected child %s at offset %s of %s when expecting %s", child, offset,
            parent, clazz);
        return clazz.cast(child);
    }

    private YangBinaryExpr parsePathPredicate(final Path_predicateContext predicate) {
        final var eqExpr = verifyNotNull(predicate.path_equality_expr());
        return YangBinaryOperator.EQUALS.exprWith(
            createChildExpr(getChild(eqExpr, 0, Node_identifierContext.class)),
            parsePathKeyExpr(verifyNotNull(eqExpr.path_key_expr())));
    }

    private YangExpr parsePathKeyExpr(final Path_key_exprContext expr) {
        final var relPath = verifyNotNull(expr.rel_path_keyexpr());
        final int children = relPath.getChildCount();

        // Process dots first
        final var steps = new ArrayList<Step>();
        int offset = 0;
        while (offset < children - 1) {
            final var child = relPath.getChild(offset);
            if (child instanceof Node_identifierContext) {
                break;
            }
            if (child instanceof TerminalNode terminal && terminal.getSymbol().getType() == PathArgLexer.DOTS) {
                steps.add(YangXPathAxis.PARENT.asStep());
            }

            ++offset;
        }

        // Process node identifiers
        while (offset < children) {
            if (relPath.getChild(offset) instanceof Node_identifierContext identifier) {
                steps.add(createChildStep(identifier, ImmutableList.of()));
            }
            ++offset;
        }

        return steps.isEmpty() ? CURRENT_CALL : YangPathExpr.of(CURRENT_CALL, YangLocationPath.relative(steps));
    }

    private YangQNameExpr createChildExpr(final Node_identifierContext qname) {
        return switch (qname.getChildCount()) {
            case 1 -> YangQNameExpr.of(Unqualified.of(qname.getText()).intern());
            case 3 -> YangQNameExpr.of(parseQName(qname));
            default -> throw new IllegalStateException("Unexpected shape " + qname.getText());
        };
    }

    private QNameStep createChildStep(final Node_identifierContext qname,
            final List<YangExpr> predicates) {
        return switch (qname.getChildCount()) {
            case 1 -> YangXPathAxis.CHILD.asStep(Unqualified.of(qname.getText()).intern(), predicates);
            case 3 -> YangXPathAxis.CHILD.asStep(parseQName(qname), predicates);
            default -> throw new IllegalStateException("Unexpected shape " + qname.getText());
        };
    }

    private @NonNull QName parseQName(final Node_identifierContext qname) {
        return ctx.identifierBinding()
            .createNodeIdentifier(ctx, qname.getChild(0).getText(), qname.getChild(2).getText());
    }
}
