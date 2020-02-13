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
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

final class StatementContextWriter implements StatementWriter {
    private final ModelProcessingPhase phase;
    private final SourceSpecificContext ctx;

    private AbstractResumedStatement<?, ?, ?> current;

    StatementContextWriter(final SourceSpecificContext ctx, final ModelProcessingPhase phase) {
        this.ctx = requireNonNull(ctx);
        this.phase = requireNonNull(phase);
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
            child.walkChildren(phase);
            child.endDeclared(phase);
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
        current = existing != null ? existing
                : verifyNotNull(ctx.createDeclaredChild(current, childId, name, argument, ref));
    }

    @Override
    public void endStatement(final StatementSourceReference ref) {
        checkState(current != null);
        current.endDeclared(phase);
        exitStatement();
    }

    @Override
    public ModelProcessingPhase getPhase() {
        return phase;
    }

    private void exitStatement() {
        // TODO: AbstractResumedStatement should only ever have AbstractResumedStatement parents, which would:
        //       - remove the StatementSource check
        //       - allow endDeclared() to be moved to AbstractResumedStatement
        //       - remove the need for verify()
        StatementContextBase<?, ?, ?> parentContext = current.getParentContext();
        while (parentContext != null && StatementSource.CONTEXT == parentContext.getStatementSource()) {
            parentContext.endDeclared(phase);
            parentContext = parentContext.getParentContext();
        }
        if (parentContext != null) {
            verify(parentContext instanceof AbstractResumedStatement, "Unexpected parent context %s", parentContext);
            current = (AbstractResumedStatement<?, ?, ?>) parentContext;
        } else {
            current = null;
        }
    }

    private static @Nullable AbstractResumedStatement<?, ?, ?> lookupDeclaredChild(
            final AbstractResumedStatement<?, ?, ?> current, final int childId) {
        if (current == null) {
            return null;
        }

        // Fast path: we are entering a statement which was emitted in previous phase
        AbstractResumedStatement<?, ?, ?> existing = current.lookupSubstatement(childId);
        while (existing != null && StatementSource.CONTEXT == existing.getStatementSource()) {
            existing = existing.lookupSubstatement(childId);
        }

        return existing;
    }
}
