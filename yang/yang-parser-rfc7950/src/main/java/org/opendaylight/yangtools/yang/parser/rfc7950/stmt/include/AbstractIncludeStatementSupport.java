/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.include;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToModuleCtx;

abstract class AbstractIncludeStatementSupport
        extends BaseStringStatementSupport<IncludeStatement, IncludeEffectiveStatement> {

    AbstractIncludeStatementSupport() {
        super(YangStmtMapping.INCLUDE);
    }

    @Override
    public final void onPreLinkageDeclared(final Mutable<String, IncludeStatement, IncludeEffectiveStatement> stmt) {
        final StmtContext<Revision, ?, ?> revision = findFirstDeclaredSubstatement(stmt,
            RevisionDateStatement.class);
        stmt.addRequiredSource(revision == null ? RevisionSourceIdentifier.create(stmt.getStatementArgument())
            : RevisionSourceIdentifier.create(stmt.getStatementArgument(), revision.getStatementArgument()));
    }

    @Override
    public final void onLinkageDeclared(final Mutable<String, IncludeStatement, IncludeEffectiveStatement> stmt) {
        final String submoduleName = stmt.coerceStatementArgument();
        final StmtContext<Revision, ?, ?> revision = findFirstDeclaredSubstatement(stmt,
            RevisionDateStatement.class);

        final ModelActionBuilder includeAction = stmt.newInferenceAction(SOURCE_LINKAGE);
        final Prerequisite<StmtContext<?, ?, ?>> requiresCtxPrerequisite;
        if (revision == null) {
            requiresCtxPrerequisite = includeAction.requiresCtx(stmt, SubmoduleNamespace.class,
                NamespaceKeyCriterion.latestRevisionModule(submoduleName), SOURCE_LINKAGE);
        } else {
            requiresCtxPrerequisite = includeAction.requiresCtx(stmt, SubmoduleNamespace.class,
                RevisionSourceIdentifier.create(submoduleName, Optional.of(revision.getStatementArgument())),
                SOURCE_LINKAGE);
        }

        includeAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final StmtContext<?, ?, ?> includedSubModuleContext = requiresCtxPrerequisite.resolve(ctx);

                stmt.addToNs(IncludedModuleContext.class, revision != null
                        ? RevisionSourceIdentifier.create(submoduleName, revision.getStatementArgument())
                                : RevisionSourceIdentifier.create(submoduleName), includedSubModuleContext);
                stmt.addToNs(IncludedSubmoduleNameToModuleCtx.class, stmt.getStatementArgument(),
                    includedSubModuleContext);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(requiresCtxPrerequisite),
                    stmt.getStatementSourceReference(),
                    "Included submodule '%s' was not found: ", stmt.getStatementArgument());
            }
        });
    }

    @Override
    protected final IncludeStatement createDeclared(final StmtContext<String, IncludeStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularIncludeStatement(ctx, substatements);
    }

    @Override
    protected final IncludeStatement createEmptyDeclared(final StmtContext<String, IncludeStatement, ?> ctx) {
        return new EmptyIncludeStatement(ctx);
    }

    @Override
    protected final IncludeEffectiveStatement createEffective(
            final StmtContext<String, IncludeStatement, IncludeEffectiveStatement> ctx,
            final IncludeStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularIncludeEffectiveStatement(declared, substatements);
    }

    @Override
    protected final IncludeEffectiveStatement createEmptyEffective(
            final StmtContext<String, IncludeStatement, IncludeEffectiveStatement> ctx,
            final IncludeStatement declared) {
        return new EmptyIncludeEffectiveStatement(declared);
    }
}
