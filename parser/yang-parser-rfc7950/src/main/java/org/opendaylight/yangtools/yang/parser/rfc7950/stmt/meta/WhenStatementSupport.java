/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

public final class WhenStatementSupport
        extends AbstractStatementSupport<QualifiedBound, WhenStatement, WhenEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.WHEN)
            .addOptional(DescriptionStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .build();

    private final @NonNull XPathSupport xpathSupport;

    public WhenStatementSupport(final XPathSupport xpathSupport, final YangParserConfiguration config) {
        // Note: if we end up binding expressions, this needs to become declaredCopy()
        super(YangStmtMapping.WHEN, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
        this.xpathSupport = requireNonNull(xpathSupport);
    }

    @Override
    public QualifiedBound parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return xpathSupport.parseXPath(ctx, value);
    }

    @Override
    protected WhenStatement createDeclared(final BoundStmtCtx<QualifiedBound> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createWhen(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected WhenStatement attachDeclarationReference(final WhenStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateWhen(stmt, reference);
    }

    @Override
    protected WhenEffectiveStatement createEffective(final Current<QualifiedBound, WhenStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createWhen(stmt.declared(), substatements);
    }
}
