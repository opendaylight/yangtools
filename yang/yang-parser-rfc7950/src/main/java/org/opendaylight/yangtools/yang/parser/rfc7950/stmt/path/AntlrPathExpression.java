/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Absolute_pathContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Descendant_pathContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Node_identifierContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Path_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Path_equality_exprContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Path_key_exprContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Path_predicateContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Rel_path_keyexprContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Relative_pathContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.PathExpression.WithLocation;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.SourceExceptionParser;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
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

final class AntlrPathExpression implements WithLocation {
    private static final YangFunctionCallExpr CURRENT_CALL = YangFunctionCallExpr.of(
        YangFunction.CURRENT.getIdentifier());

    private final @NonNull YangLocationPath location;
    private final @NonNull String originalString;

    private AntlrPathExpression(final YangLocationPath location, final String originalString) {
        this.location = requireNonNull(location);
        this.originalString = requireNonNull(originalString);
    }

    @Override
    public YangLocationPath getLocation() {
        return location;
    }

    @Override
    public String getOriginalString() {
        return originalString;
    }

    static AntlrPathExpression parse(final StmtContext<?, ?, ?> ctx, final String pathArg) {
        final LeafRefPathLexer lexer = new LeafRefPathLexer(CharStreams.fromString(pathArg));
        final LeafRefPathParser parser = new LeafRefPathParser(new CommonTokenStream(lexer));
        final Path_argContext path = SourceExceptionParser.parse(lexer, parser, parser::path_arg,
            ctx.getStatementSourceReference());

        final ParseTree childPath = path.getChild(0);
        final YangLocationPath location;
        if (childPath instanceof Absolute_pathContext) {
            location = parseAbsolute(ctx, pathArg, (Absolute_pathContext) childPath);
        } else if (childPath instanceof Relative_pathContext) {
            location = parseRelative(ctx, pathArg, (Relative_pathContext) childPath);
        } else {
            throw new IllegalStateException("Unsupported child " + childPath);
        }

        return new AntlrPathExpression(location, pathArg);
    }

    private static Absolute parseAbsolute(final StmtContext<?, ?, ?> ctx, final String pathArg,
            final Absolute_pathContext absolute) {
        final List<Step> steps = new ArrayList<>();
        fillSteps(ctx, pathArg, absolute, steps);
        return YangLocationPath.absolute(steps);
    }

    private static Relative parseRelative(final StmtContext<?, ?, ?> ctx, final String pathArg,
            final Relative_pathContext relative) {
        final List<Step> steps = new ArrayList<>();

        final int relativeChildren = relative.getChildCount();
        verify(relativeChildren % 2 == 1, "Unexpected child count %s", relativeChildren);
        for (int i = 0; i < relativeChildren / 2; ++i) {
            steps.add(YangXPathAxis.PARENT.asStep());
        }

        final Descendant_pathContext descendant = getChild(relative, relativeChildren - 1,
            Descendant_pathContext.class);
        final Node_identifierContext qname = getChild(descendant, 0, Node_identifierContext.class);
        final int descandantChildren = descendant.getChildCount();
        if (descandantChildren > 1) {
            final List<YangExpr> predicates = new ArrayList<>(descandantChildren);
            for (int i = 1; i < descandantChildren - 1; ++i) {
                predicates.add(parsePathPredicate(ctx, getChild(descendant, i, Path_predicateContext.class)));
            }
            steps.add(createChildStep(ctx, qname, predicates));
            fillSteps(ctx, pathArg, getChild(descendant, descandantChildren - 1, Absolute_pathContext.class), steps);
        } else {
            steps.add(createChildStep(ctx, qname, ImmutableList.of()));
        }

        return YangLocationPath.relative(steps);
    }

    private static void fillSteps(final StmtContext<?, ?, ?> ctx, final String pathArg,
            final Absolute_pathContext absolute, final List<Step> output) {

        final List<YangExpr> predicates = new ArrayList<>();
        Node_identifierContext qname = getChild(absolute, 1, Node_identifierContext.class);

        final int children = absolute.getChildCount();
        for (int i = 2; i < children; ++i) {
            final ParseTree child = absolute.getChild(i);
            if (child instanceof Node_identifierContext) {
                output.add(createChildStep(ctx, qname, predicates));
                predicates.clear();
                qname = (Node_identifierContext) child;
            } else if (child instanceof Path_predicateContext) {
                predicates.add(parsePathPredicate(ctx, (Path_predicateContext) child));
            }
        }

        output.add(createChildStep(ctx, qname, predicates));
    }

    private static <T> T getChild(final ParseTree parent, final int offset, final Class<T> clazz) {
        final ParseTree child = parent.getChild(offset);
        verify(clazz.isInstance(child), "Unexpected child %s at offset %s of %s when expecting %s", child, offset,
            parent, clazz);
        return clazz.cast(child);
    }

    private static YangBinaryExpr parsePathPredicate(final StmtContext<?, ?, ?> ctx,
            final Path_predicateContext predicate) {
        final Path_equality_exprContext eqExpr = verifyNotNull(predicate.path_equality_expr());
        return YangBinaryOperator.EQUALS.exprWith(
            createChildExpr(ctx, getChild(eqExpr, 0, Node_identifierContext.class)),
            parsePathKeyExpr(ctx, verifyNotNull(eqExpr.path_key_expr())));
    }

    private static YangExpr parsePathKeyExpr(final StmtContext<?, ?, ?> ctx, final Path_key_exprContext expr) {
        final Rel_path_keyexprContext relPath = verifyNotNull(expr.rel_path_keyexpr());
        final int children = relPath.getChildCount();

        // Process dots first
        final List<Step> steps = new ArrayList<>();
        int offset = 0;
        while (offset < children - 1) {
            final ParseTree child = expr.getChild(offset);
            if (child instanceof Node_identifierContext) {
                break;
            }
            if (child instanceof TerminalNode
                    && ((TerminalNode) child).getSymbol().getType() == LeafRefPathLexer.DOTS) {
                steps.add(YangXPathAxis.PARENT.asStep());
            }

            ++offset;
        }

        // Process node identifiers
        while (offset < children) {
            final ParseTree child = expr.getChild(offset);
            if (child instanceof Node_identifierContext) {
                steps.add(createChildStep(ctx, (Node_identifierContext) child, ImmutableList.of()));
            }
            ++offset;
        }

        return steps.isEmpty() ? CURRENT_CALL : YangPathExpr.of(CURRENT_CALL, YangLocationPath.relative(steps));
    }

    private static YangQNameExpr createChildExpr(final StmtContext<?, ?, ?> ctx, final Node_identifierContext qname) {
        switch (qname.getChildCount()) {
            case 1:
                return YangQNameExpr.of(UnqualifiedQName.of(qname.getText()).intern());
            case 3:
                return YangQNameExpr.of(parseQName(ctx, qname));
            default:
                throw new IllegalStateException("Unexpected shape " + qname.getText());
        }
    }

    private static QNameStep createChildStep(final StmtContext<?, ?, ?> ctx, final Node_identifierContext qname,
            final List<YangExpr> predicates) {
        switch (qname.getChildCount()) {
            case 1:
                return YangXPathAxis.CHILD.asStep(UnqualifiedQName.of(qname.getText()).intern(), predicates);
            case 3:
                return YangXPathAxis.CHILD.asStep(parseQName(ctx, qname), predicates);
            default:
                throw new IllegalStateException("Unexpected shape " + qname.getText());
        }
    }

    private static QName parseQName(final StmtContext<?, ?, ?> ctx, final Node_identifierContext qname) {
        return StmtContextUtils.parseNodeIdentifier(ctx, qname.getChild(0).getText(), qname.getChild(2).getText());
    }
}
