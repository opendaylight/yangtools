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

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

final class StatementContextWriter implements StatementWriter {
    private final ModelProcessingPhase phase;
    private final SourceSpecificContext ctx;

    private StatementContextBase<?, ?, ?> current;

    StatementContextWriter(final SourceSpecificContext ctx, final ModelProcessingPhase phase) {
        this.ctx = requireNonNull(ctx);
        this.phase = requireNonNull(phase);
    }

    @Override
    public Optional<? extends ResumedStatement> resumeStatement(final int childId) {
        final Optional<StatementContextBase<?, ?, ?>> existing = ctx.lookupDeclaredChild(current, childId);
        existing.ifPresent(this::resumeStatement);
        return existing;
    }

    private void resumeStatement(final StatementContextBase<?, ?, ?> child) {
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
        final Optional<StatementContextBase<?, ?, ?>> existing = ctx.lookupDeclaredChild(current, childId);
        current = existing.isPresent() ? existing.get()
                :  verifyNotNull(ctx.createDeclaredChild(current, childId, name, argument, ref));
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
        StatementContextBase<?, ?, ?> parentContext = current.getParentContext();
        while (parentContext != null && StatementSource.CONTEXT == parentContext.getStatementSource()) {
            parentContext.endDeclared(phase);
            parentContext = parentContext.getParentContext();
        }
        current = parentContext;
    }
}
