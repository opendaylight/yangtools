/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class PathStatementSupport
        extends BaseStatementSupport<PathExpression, PathStatement, PathEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.PATH).build();
    private static final PathStatementSupport LENIENT_INSTANCE = new PathStatementSupport(
        new PathExpressionParser.Lenient());
    private static final PathStatementSupport STRICT_INSTANCE = new PathStatementSupport(
        new PathExpressionParser());

    private final PathExpressionParser parser;

    private PathStatementSupport(final PathExpressionParser parser) {
        super(YangStmtMapping.PATH);
        this.parser = requireNonNull(parser);
    }

    public static PathStatementSupport lenientInstance() {
        return LENIENT_INSTANCE;
    }

    public static PathStatementSupport strictInstance() {
        return STRICT_INSTANCE;
    }

    @Override
    public PathExpression parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return parser.parseExpression(ctx, value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected PathStatement createDeclared(final StmtContext<PathExpression, PathStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularPathStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected PathStatement createEmptyDeclared(final StmtContext<PathExpression, PathStatement, ?> ctx) {
        return new EmptyPathStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected PathEffectiveStatement createEffective(
            final StmtContext<PathExpression, PathStatement, PathEffectiveStatement> ctx,
            final PathStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularPathEffectiveStatement(declared, substatements);
    }

    @Override
    protected PathEffectiveStatement createEmptyEffective(
            final StmtContext<PathExpression, PathStatement, PathEffectiveStatement> ctx,
            final PathStatement declared) {
        return new EmptyPathEffectiveStatement(declared);
    }
}