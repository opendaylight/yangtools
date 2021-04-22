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
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class PathStatementSupport
        extends AbstractStatementSupport<PathExpression, PathStatement, PathEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.PATH).build();

    private final PathExpressionParser parser;

    private PathStatementSupport(final YangParserConfiguration config, final PathExpressionParser parser) {
        // TODO: can 'path' really be copied?
        super(YangStmtMapping.PATH, StatementPolicy.contextIndependent(), config);
        this.parser = requireNonNull(parser);
    }

    public static PathStatementSupport lenientInstance(final YangParserConfiguration config) {
        return new PathStatementSupport(config, new PathExpressionParser.Lenient());
    }

    public static PathStatementSupport strictInstance(final YangParserConfiguration config) {
        return new PathStatementSupport(config, new PathExpressionParser());
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
        return DeclaredStatements.createPath(ctx.getArgument(), substatements);
    }

    @Override
    protected PathStatement attachDeclarationReference(final PathStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decoratePath(stmt, reference);
    }

    @Override
    protected PathEffectiveStatement createEffective(final Current<PathExpression, PathStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createPath(stmt.declared(), substatements);
    }
}
