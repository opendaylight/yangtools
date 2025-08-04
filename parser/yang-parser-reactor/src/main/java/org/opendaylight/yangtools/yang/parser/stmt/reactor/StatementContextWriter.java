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
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

final class StatementContextWriter implements StatementWriter {
    private final @NonNull ModelProcessingPhase phase;
    private final ResolvedSource resolvedsource;
    private final Map<QNameModule, RootStatementContext<?, ?, ?>> qNameToRootStmt;
    private final SourceSpecificContext ctx;

    private AbstractResumedStatement<?, ?, ?> current;

    StatementContextWriter(final SourceSpecificContext ctx, final ModelProcessingPhase phase,
        final ResolvedSource resolvedsource, final Map<QNameModule, RootStatementContext<?,?,?>> qNameToRootStmt) {
        this.ctx = requireNonNull(ctx);
        this.phase = requireNonNull(phase);
        this.resolvedsource = resolvedsource;
        this.qNameToRootStmt = qNameToRootStmt;
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
                fillNamespaces(rootStatement);
                qNameToRootStmt.put(resolvedsource.getQNameModule(), rootStatement);
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

        rootContext.addToNamespace(ParserNamespaces.MODULECTX_TO_QNAME, rootContext, resolvedsource.getQNameModule());
        for (Map.Entry<String, ResolvedSource> imported : resolvedsource.getImports().entrySet()) {
            final QNameModule importedModuleQName = imported.getValue().getQNameModule();
            rootContext.addToNamespace(ParserNamespaces.IMPORT_PREFIX_TO_QNAME_MODULE, imported.getKey(),
                importedModuleQName);
            final var importedContext = verifyNotNull(qNameToRootStmt.get(importedModuleQName),
                "Root context of module %s imported by %s was not resolved yet", importedModuleQName,
                resolvedsource.getQNameModule());
            rootContext.addToNamespace(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, imported.getKey(),
                importedContext);

//            rootContext.addToNamespace(ModuleQNameToPrefix.INSTANCE); // requires dependency on yang-parser-rfc7950 and
            // may not be necessary anyway. We might be good with just IMPORT_PREFIX_TO_QNAME_MODULE
        }


        if (resolvedsource.getBelongsTo() != null) {
            rootContext.addToNamespace(ParserNamespaces.BELONGSTO_PREFIX_TO_QNAME_MODULE,
                resolvedsource.getBelongsTo().getKey(), resolvedsource.getBelongsTo().getValue());
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
