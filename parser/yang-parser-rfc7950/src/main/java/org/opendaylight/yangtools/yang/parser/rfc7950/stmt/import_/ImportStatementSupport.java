/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_PRE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.YangVersionLinkageException;

@Beta
public final class ImportStatementSupport
        extends AbstractUnqualifiedStatementSupport<ImportStatement, ImportEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.IMPORT)
            .addMandatory(YangStmtMapping.PREFIX)
            .addOptional(YangStmtMapping.REVISION_DATE)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.IMPORT)
            .addMandatory(YangStmtMapping.PREFIX)
            .addOptional(YangStmtMapping.REVISION_DATE)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .build();

    private ImportStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.IMPORT, StatementPolicy.reject(), config, validator);
    }

    public static @NonNull ImportStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new ImportStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull ImportStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new ImportStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public void onPreLinkageDeclared(final Mutable<Unqualified, ImportStatement, ImportEffectiveStatement> stmt) {
        /*
         * Add ModuleIdentifier of a module which is required by this module.
         * Based on this information, required modules are searched from library
         * sources.
         */
        final var revision = StmtContextUtils.findFirstDeclaredSubstatement(stmt, RevisionDateStatement.class);

        final Unqualified moduleName = stmt.getArgument();
        final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_PRE_LINKAGE);
        final var imported = importAction.requiresCtx(stmt, ParserNamespaces.PRELINKAGE_MODULE, moduleName,
            SOURCE_PRE_LINKAGE);
        final Prerequisite<Mutable<?, ?, ?>> rootPrereq = importAction.mutatesCtx(stmt.getRoot(), SOURCE_PRE_LINKAGE);

        importAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final StmtContext<?, ?, ?> importedModuleContext = imported.resolve(ctx);
                verify(moduleName.equals(importedModuleContext.getArgument()));
                final XMLNamespace importedModuleNamespace = verifyNotNull(importedModuleContext.namespaceItem(
                    ParserNamespaces.MODULE_NAME_TO_NAMESPACE, moduleName));
                final String impPrefix = SourceException.throwIfNull(
                    firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class), stmt,
                    "Missing prefix statement");

                final Mutable<?, ?, ?> root = rootPrereq.resolve(ctx);
                // Version 1 sources must not import-by-revision Version 1.1 modules
                if (revision != null && root.yangVersion() == YangVersion.VERSION_1) {
                    final YangVersion importedVersion = importedModuleContext.yangVersion();
                    if (importedVersion != YangVersion.VERSION_1) {
                        throw new YangVersionLinkageException(stmt, "Cannot import by revision version %s module %s",
                            importedVersion, moduleName.getLocalName());
                    }
                }

                stmt.addToNs(ParserNamespaces.IMP_PREFIX_TO_NAMESPACE, impPrefix, importedModuleNamespace);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(imported), stmt, "Imported module [%s] was not found.",
                    moduleName.getLocalName());
            }
        });
    }

    @Override
    public void onLinkageDeclared(final Mutable<Unqualified, ImportStatement, ImportEffectiveStatement> stmt) {
        RevisionImport.onLinkageDeclared(stmt);
    }

    @Override
    protected ImportStatement createDeclared(final BoundStmtCtx<Unqualified> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createImport(ctx.getArgument(), substatements);
    }

    @Override
    protected ImportStatement attachDeclarationReference(final ImportStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateImport(stmt, reference);
    }

    @Override
    protected ImportEffectiveStatement createEffective(final Current<Unqualified, ImportStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        InferenceException.throwIf(substatements.isEmpty(), stmt, "Unexpected empty effective import statement");
        return EffectiveStatements.createImport(stmt.declared(), substatements,
            verifyNotNull(stmt.namespaceItem(ImportedVersionNamespace.INSTANCE, Empty.value())));
    }
}
