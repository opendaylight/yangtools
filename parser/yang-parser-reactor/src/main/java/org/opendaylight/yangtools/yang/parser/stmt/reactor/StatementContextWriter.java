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
    private final @NonNull ResolvedSource resolvedSource;
    private final Map<SourceIdentifier, Map.Entry<RootStatementContext<?, ?, ?>, ResolvedSource>> resolvedRootContexts;
    private final @NonNull SourceSpecificContext ctx;

    private AbstractResumedStatement<?, ?, ?> current;

    StatementContextWriter(final SourceSpecificContext ctx, final ModelProcessingPhase phase,
        final ResolvedSource resolvedSource,
        final Map<SourceIdentifier, Map.Entry<RootStatementContext<?, ?, ?>, ResolvedSource>> resolvedRootContexts) {
        this.ctx = requireNonNull(ctx);
        this.phase = requireNonNull(phase);
        this.resolvedSource = requireNonNull(resolvedSource);
        this.resolvedRootContexts = requireNonNull(resolvedRootContexts);
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
        if (existing != null) {
            current = existing;
            return;
        }

        final var newStmt = verifyNotNull(
            ctx.createDeclaredChild(current, childId, name, argument, ref));

        if (newStmt instanceof RootStatementContext<?, ?, ?> root) {
            updateRootVersionIfNeeded(root);
            populateRootNamespaces(root);
            resolvedRootContexts.put(resolvedSource.getSourceId(), Map.entry(root, resolvedSource));
        }

        current = newStmt;
    }

    private void updateRootVersionIfNeeded(final RootStatementContext<?, ?, ?> root) {
        if (!root.yangVersion().equals(resolvedSource.getYangVersion())) {
            root.setRootVersionImpl(resolvedSource.getYangVersion());
        }
    }

    /**
     * Populates namespace mappings for the root module/submodule.
     */
    private void populateRootNamespaces(final RootStatementContext<?, ?, ?> root) {
        fillModuleNamespace(root);
        fillImportedNamespaces(root);

        if (resolvedSource.getBelongsTo() != null) {
            fillSubmoduleNamespaces(root);
        } else {
            fillModuleRootNamespaces(root);
        }
    }

    private void fillModuleNamespace(final RootStatementContext<?, ?, ?> root) {
        root.addToNamespace(ParserNamespaces.MODULECTX_TO_QNAME, root, resolvedSource.getQNameModule());
    }

    private void fillImportedNamespaces(final RootStatementContext<?, ?, ?> root) {
        for (var entry : resolvedSource.getImports().entrySet()) {
            final String prefix = entry.getKey();
            final ResolvedSource imported = entry.getValue();
            final QNameModule qnameModule = imported.getQNameModule();
            final SourceIdentifier sourceId = imported.getSourceId();

            Map.Entry<RootStatementContext<?, ?, ?>, ResolvedSource> importedContext =
                verifyNotNull(resolvedRootContexts.get(sourceId),
                "Root context of imported module %s (imported by %s) was not resolved",
                qnameModule, resolvedSource.getQNameModule());

            root.addToNamespace(ParserNamespaces.IMPORT_PREFIX_TO_QNAME_MODULE, prefix, qnameModule);
            root.addToNamespace(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, prefix, importedContext.getKey());
            root.addToNamespace(ParserNamespaces.IMPORT_PREFIX_TO_SOURCE_ID, prefix, sourceId);
            root.addToNamespace(ParserNamespaces.IMPORTED_MODULE, sourceId, importedContext.getKey());
        }
    }

    private void fillSubmoduleNamespaces(final RootStatementContext<?, ?, ?> root) {
        root.addToNs(ParserNamespaces.SUBMODULE, root.getRootIdentifier(),
            (StmtContext<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement>) root);

        final var belongsTo = resolvedSource.getBelongsTo();
        root.addToNamespace(ParserNamespaces.BELONGSTO_PREFIX_TO_QNAME_MODULE, belongsTo.getKey(),
            belongsTo.getValue());
    }

    private void fillModuleRootNamespaces(final RootStatementContext<?, ?, ?> root) {
        root.addToNs(ParserNamespaces.MODULE, root.getRootIdentifier(),
            (StmtContext<Unqualified, ModuleStatement, ModuleEffectiveStatement>) root);

        root.addToNs(ParserNamespaces.IMPORT_PREFIX_TO_QNAME_MODULE, resolvedSource.getPrefix(),
            resolvedSource.getQNameModule());
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
