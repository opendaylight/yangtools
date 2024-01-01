/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractUnqualifiedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.YangVersionLinkageException;

@Beta
public final class IncludeStatementSupport
        extends AbstractUnqualifiedStatementSupport<IncludeStatement, IncludeEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.INCLUDE)
            .addOptional(YangStmtMapping.REVISION_DATE)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.INCLUDE)
            .addOptional(YangStmtMapping.REVISION_DATE)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .build();

    IncludeStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.INCLUDE, StatementPolicy.reject(), config, validator);
    }

    public static @NonNull IncludeStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new IncludeStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull IncludeStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new IncludeStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public void onLinkageDeclared(final Mutable<Unqualified, IncludeStatement, IncludeEffectiveStatement> stmt) {
        final Unqualified submoduleName = stmt.getArgument();
        final StmtContext<Revision, ?, ?> revision = findFirstDeclaredSubstatement(stmt, RevisionDateStatement.class);

        final ModelActionBuilder includeAction = stmt.newInferenceAction(SOURCE_LINKAGE);
        final Prerequisite<StmtContext<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement>>
            requiresCtxPrerequisite;
        if (revision == null) {
            requiresCtxPrerequisite = includeAction.requiresCtx(stmt, ParserNamespaces.SUBMODULE,
                NamespaceKeyCriterion.latestRevisionModule(submoduleName), SOURCE_LINKAGE);
        } else {
            requiresCtxPrerequisite = includeAction.requiresCtx(stmt, ParserNamespaces.SUBMODULE,
                new SourceIdentifier(submoduleName, revision.argument()), SOURCE_LINKAGE);
        }

        includeAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final StmtContext<?, ?, ?> includedSubModuleContext = requiresCtxPrerequisite.resolve(ctx);
                final YangVersion modVersion = stmt.getRoot().yangVersion();
                final YangVersion subVersion = includedSubModuleContext.yangVersion();
                if (subVersion != modVersion) {
                    throw new YangVersionLinkageException(stmt,
                        "Cannot include a version %s submodule in a version %s module", subVersion, modVersion);
                }

                stmt.addToNs(ParserNamespaces.INCLUDED_MODULE,
                    new SourceIdentifier(submoduleName, revision != null ? revision.getArgument() : null),
                    includedSubModuleContext);
                stmt.addToNs(ParserNamespaces.INCLUDED_SUBMODULE_NAME_TO_MODULECTX, stmt.argument(),
                    includedSubModuleContext);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(requiresCtxPrerequisite), stmt,
                    "Included submodule '%s' was not found", stmt.rawArgument());
            }
        });
    }

    @Override
    protected IncludeStatement createDeclared(final BoundStmtCtx<Unqualified> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createInclude(ctx.getArgument(), substatements);
    }

    @Override
    protected IncludeStatement attachDeclarationReference(final IncludeStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateInclude(stmt, reference);
    }

    @Override
    protected IncludeEffectiveStatement createEffective(final Current<Unqualified, IncludeStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createInclude(stmt.declared(), substatements);
    }
}
