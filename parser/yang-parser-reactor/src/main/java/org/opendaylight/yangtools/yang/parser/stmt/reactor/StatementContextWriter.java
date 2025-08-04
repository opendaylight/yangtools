/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

final class StatementContextWriter implements StatementWriter {
    private final @NonNull ModelProcessingPhase phase;
    private final ResolvedSource resolvedsource;
    private final Map<ResolvedSource, RootStatementContext<?, ?, ?>> resolvedRootContexts;
    private final SourceSpecificContext ctx;

    private AbstractResumedStatement<?, ?, ?> current;

    StatementContextWriter(final SourceSpecificContext ctx, final ModelProcessingPhase phase,
        final ResolvedSource resolvedsource, final Map<ResolvedSource, RootStatementContext<?,?,?>> resolvedRootContexts) {
        this.ctx = requireNonNull(ctx);
        this.phase = requireNonNull(phase);
        this.resolvedsource = resolvedsource;
        this.resolvedRootContexts = resolvedRootContexts;
    }

    @Override
    public ModelProcessingPhase getPhase() {
        return phase;
    }

    @Override
    public Optional<? extends ResumedStatement> resumeStatement(final int childId) {
        final AbstractResumedStatement<?, ?, ?> existing = lookupDeclaredChild(current, childId);
        if (existing != null) {
            resumeStatement(existing);
            return Optional.of(existing);
        }
        return Optional.empty();
    }

    private void resumeStatement(final AbstractResumedStatement<?, ?, ?> child) {
        if (child.isFullyDefined()) {
            child.declarationFinished(phase);
        } else {
            current = child;
        }
    }

    @Override
    public void storeStatement(final int expectedChildren, final boolean fullyDefined) {
        checkState(current != null);
        checkArgument(expectedChildren >= 0);
        current.resizeSubstatements(expectedChildren);

        if (fullyDefined) {
            current.setFullyDefined();
        }
    }

    @Override
    public void startStatement(final int childId, final QName name, final String argument,
        final StatementSourceReference ref) {
        final AbstractResumedStatement<?, ?, ?> existing = lookupDeclaredChild(current, childId);
        if (existing == null) {
            var newStatement = verifyNotNull(ctx.createDeclaredChild(current, childId, name, argument, ref));
            if (newStatement instanceof RootStatementContext<?,?,?> rootStatement) {
                if (!rootStatement.yangVersion().equals(resolvedsource.getYangVersion())) {
                    rootStatement.setRootVersionImpl(resolvedsource.getYangVersion());
                }
                fillNamespaces(rootStatement);
                resolvedRootContexts.put(resolvedsource, rootStatement);
            }
            current = newStatement;
        } else {
            current = existing;
        }
    }

    /**
     * Fills the namespaces of the Root context. These are later used to construct substatements like import, include,
     * belongs-to, which rely on data from other modules.
     * @param rootContext the root (module/submodule) context
     */
    private void fillNamespaces(final RootStatementContext<?,?,?> rootContext) {

        //TODO: tidy up
        rootContext.addToNamespace(ParserNamespaces.MODULECTX_TO_QNAME, rootContext, resolvedsource.getQNameModule());

        for (Map.Entry<String, ResolvedSource> imported : resolvedsource.getImports().entrySet()) {
            final QNameModule importedModuleQName = imported.getValue().getQNameModule();
            final SourceIdentifier importedSourceId = imported.getValue().getSourceId();
            final var importedContext = verifyNotNull(resolvedRootContexts.get(imported.getValue()),
                "Root context of module %s imported by %s was not resolved yet", importedModuleQName,
                resolvedsource.getQNameModule());

            rootContext.addToNamespace(ParserNamespaces.IMPORT_PREFIX_TO_QNAME_MODULE, imported.getKey(),
                importedModuleQName);
            rootContext.addToNamespace(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, imported.getKey(),
                importedContext);
            rootContext.addToNamespace(ParserNamespaces.IMPORT_PREFIX_TO_SOURCE_ID, imported.getKey(),
                importedSourceId);
            rootContext.addToNamespace(ParserNamespaces.IMPORTED_MODULE, importedSourceId, importedContext);
        }

        for (Map.Entry<ResolvedSource, Unqualified> anInclude : resolvedsource.getIncludes().entrySet()) {
            final ResolvedSource include = anInclude.getKey();
            final QNameModule includedQNameModule = include.getQNameModule();
            final var includedContext = verifyNotNull(resolvedRootContexts.get(include),
                "Root context of module %s included by %s was not resolved yet", includedQNameModule,
                resolvedsource.getQNameModule());

            rootContext.addToNs(ParserNamespaces.INCLUDED_MODULE, include.getContext().getRootIdentifier(),
                includedContext);
            rootContext.addToNs(ParserNamespaces.INCLUDED_SUBMODULE_NAME_TO_MODULECTX, anInclude.getValue(),
                includedContext);
        }

        if (resolvedsource.getBelongsTo() != null) {
            rootContext.addToNs(ParserNamespaces.SUBMODULE, rootContext.getRootIdentifier(),
                (StmtContext<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement>) rootContext);
            rootContext.addToNamespace(ParserNamespaces.BELONGSTO_PREFIX_TO_QNAME_MODULE,
                resolvedsource.getBelongsTo().getKey(), resolvedsource.getBelongsTo().getValue());
            // we might need the BELONGSTO_PREFIX_TO_MODULECTX in SourceSpecificContext.updateImportedNamespaces().
            // if so, we need to iterate over all the RootStatements according to dependency tree - from roots to leafs
        } else {
            // this is a module root context (no belongs-to).
            rootContext.addToNs(ParserNamespaces.MODULE, rootContext.getRootIdentifier(),
                (StmtContext<Unqualified, ModuleStatement, ModuleEffectiveStatement>) rootContext);
            rootContext.addToNs(ParserNamespaces.IMPORT_PREFIX_TO_QNAME_MODULE, resolvedsource.getPrefix(),
                resolvedsource.getQNameModule());
        }
    }

    @Override
    public void endStatement(final StatementSourceReference ref) {
        checkState(current != null);
        current = current.exitStatement(phase);
    }

    private static @Nullable AbstractResumedStatement<?, ?, ?> lookupDeclaredChild(
        final AbstractResumedStatement<?, ?, ?> current, final int childId) {
        return current == null ? null : current.enterSubstatement(childId);
    }
}
