/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.rfc8791.model.api.YangDataStructureStatements;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractAugmentStructureStatementSupport
        extends BaseStatementSupport<Absolute, AugmentStructureStatement, AugmentStructureEffectiveStatement> {
    AbstractAugmentStructureStatementSupport() {
        super(YangDataStructureStatements.STRUCTURE);
    }

    @Override
    public final Absolute parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final SchemaNodeIdentifier arg = ArgumentUtils.nodeIdentifierFromPath(ctx, value);
        SourceException.throwIf(!(arg instanceof Absolute), ctx.getStatementSourceReference(),
            "Argument '%s' is not an absolute schema node identifier", value);
        return (Absolute) arg;
    }
    @Override
    protected final AugmentStructureStatement createDeclared(
            final StmtContext<Absolute, AugmentStructureStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected final AugmentStructureStatement createEmptyDeclared(
            final StmtContext<Absolute, AugmentStructureStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected final AugmentStructureEffectiveStatement createEffective(
            final StmtContext<Absolute, AugmentStructureStatement, AugmentStructureEffectiveStatement> ctx,
            final AugmentStructureStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected final  AugmentStructureEffectiveStatement createEmptyEffective(
            final StmtContext<Absolute, AugmentStructureStatement, AugmentStructureEffectiveStatement> ctx,
            final AugmentStructureStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
