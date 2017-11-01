/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToModuleCtx;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IncludeEffectiveStatementImpl;

public class IncludeStatementImpl extends AbstractDeclaredStatement<String> implements IncludeStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.INCLUDE).addOptional(YangStmtMapping.REVISION_DATE).build();

    protected IncludeStatementImpl(final StmtContext<String, IncludeStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, IncludeStatement, EffectiveStatement<String, IncludeStatement>> {

        public Definition() {
            super(YangStmtMapping.INCLUDE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public IncludeStatement createDeclared(final StmtContext<String, IncludeStatement, ?> ctx) {
            return new IncludeStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, IncludeStatement> createEffective(
                final StmtContext<String, IncludeStatement, EffectiveStatement<String, IncludeStatement>> ctx) {
            return new IncludeEffectiveStatementImpl(ctx);
        }

        @Override
        public void onPreLinkageDeclared(
                final Mutable<String, IncludeStatement, EffectiveStatement<String, IncludeStatement>> stmt) {
            final StmtContext<Revision, ?, ?> revision = findFirstDeclaredSubstatement(stmt,
                RevisionDateStatement.class);
            stmt.addRequiredSource(revision == null ? RevisionSourceIdentifier.create(stmt.getStatementArgument())
                : RevisionSourceIdentifier.create(stmt.getStatementArgument(), revision.getStatementArgument()));
        }

        @Override
        public void onLinkageDeclared(
                final Mutable<String, IncludeStatement, EffectiveStatement<String, IncludeStatement>> stmt) {
            final String submoduleName = stmt.getStatementArgument();
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
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getModule() {
        return argument();
    }

    @Override
    public RevisionDateStatement getRevisionDate() {
        return firstDeclared(RevisionDateStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

}
