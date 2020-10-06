/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractSubmoduleStatementSupport
        extends BaseStatementSupport<UnqualifiedQName, SubmoduleStatement, SubmoduleEffectiveStatement> {
    AbstractSubmoduleStatementSupport() {
        super(YangStmtMapping.SUBMODULE);
    }

    @Override
    public final UnqualifiedQName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return UnqualifiedQName.of(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(e.getMessage(), ctx.getStatementSourceReference(), e);
        }
    }

    @Override
    public final void onPreLinkageDeclared(
            final Mutable<UnqualifiedQName, SubmoduleStatement, SubmoduleEffectiveStatement> stmt) {
        stmt.setRootIdentifier(RevisionSourceIdentifier.create(stmt.coerceStatementArgument().getLocalName(),
            StmtContextUtils.getLatestRevision(stmt.declaredSubstatements())));
    }

    @Override
    public final void onLinkageDeclared(
            final Mutable<UnqualifiedQName, SubmoduleStatement, SubmoduleEffectiveStatement> stmt) {
        final SourceIdentifier submoduleIdentifier = RevisionSourceIdentifier.create(
            stmt.coerceStatementArgument().getLocalName(),
            StmtContextUtils.getLatestRevision(stmt.declaredSubstatements()));

        final StmtContext<?, SubmoduleStatement, SubmoduleEffectiveStatement>
            possibleDuplicateSubmodule = stmt.getFromNamespace(SubmoduleNamespace.class, submoduleIdentifier);
        if (possibleDuplicateSubmodule != null && possibleDuplicateSubmodule != stmt) {
            throw new SourceException(stmt.getStatementSourceReference(), "Submodule name collision: %s. At %s",
                    stmt.getStatementArgument(), possibleDuplicateSubmodule.getStatementSourceReference());
        }

        stmt.addContext(SubmoduleNamespace.class, submoduleIdentifier, stmt);

        final String belongsToModuleName = firstAttributeOf(stmt.declaredSubstatements(), BelongsToStatement.class);
        final StmtContext<?, ?, ?> prefixSubStmtCtx = findFirstDeclaredSubstatement(stmt, 0,
                BelongsToStatement.class, PrefixStatement.class);
        SourceException.throwIfNull(prefixSubStmtCtx, stmt.getStatementSourceReference(),
                "Prefix of belongsTo statement is missing in submodule [%s]", stmt.getStatementArgument());

        final String prefix = (String) prefixSubStmtCtx.getStatementArgument();

        stmt.addToNs(BelongsToPrefixToModuleName.class, prefix, belongsToModuleName);
    }

    @Override
    protected final SubmoduleStatement createDeclared(final StmtContext<UnqualifiedQName, SubmoduleStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new SubmoduleStatementImpl(ctx, substatements);
    }

    @Override
    protected final SubmoduleStatement createEmptyDeclared(
            final StmtContext<UnqualifiedQName, SubmoduleStatement, ?> ctx) {
        throw noBelongsTo(ctx);
    }

    @Override
    protected final SubmoduleEffectiveStatement createEffective(
            final StmtContext<UnqualifiedQName, SubmoduleStatement, SubmoduleEffectiveStatement> ctx,
            final SubmoduleStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new SubmoduleEffectiveStatementImpl(ctx, declared, substatements);
    }

    @Override
    protected final SubmoduleEffectiveStatement createEmptyEffective(
            final StmtContext<UnqualifiedQName, SubmoduleStatement, SubmoduleEffectiveStatement> ctx,
            final SubmoduleStatement declared) {
        throw noBelongsTo(ctx);
    }

    private static SourceException noBelongsTo(final StmtContext<?, ?, ?> ctx) {
        return new SourceException("No belongs-to declared in submodule", ctx.getStatementSourceReference());
    }
}
