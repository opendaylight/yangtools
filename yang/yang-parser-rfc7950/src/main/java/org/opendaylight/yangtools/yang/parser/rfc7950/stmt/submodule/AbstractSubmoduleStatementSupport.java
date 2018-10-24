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

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractSubmoduleStatementSupport
        extends AbstractStatementSupport<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> {
    AbstractSubmoduleStatementSupport() {
        super(YangStmtMapping.SUBMODULE);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public final SubmoduleStatement createDeclared(final StmtContext<String, SubmoduleStatement, ?> ctx) {
        return new SubmoduleStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<String, SubmoduleStatement> createEffective(
            final StmtContext<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> ctx) {
        return new SubmoduleEffectiveStatementImpl(ctx);
    }

    @Override
    public final void onPreLinkageDeclared(
            final Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> stmt) {
        stmt.setRootIdentifier(RevisionSourceIdentifier.create(stmt.getStatementArgument(),
            StmtContextUtils.getLatestRevision(stmt.declaredSubstatements())));
    }

    @Override
    public final void onLinkageDeclared(
            final Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> stmt) {
        final SourceIdentifier submoduleIdentifier = RevisionSourceIdentifier.create(stmt.coerceStatementArgument(),
            StmtContextUtils.getLatestRevision(stmt.declaredSubstatements()));

        final StmtContext<?, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>>
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
}