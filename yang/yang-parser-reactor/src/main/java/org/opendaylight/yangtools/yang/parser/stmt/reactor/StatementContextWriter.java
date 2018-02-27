/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.Optional;
import javax.annotation.Nonnull;
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
        this.ctx = Preconditions.checkNotNull(ctx);
        this.phase = Preconditions.checkNotNull(phase);
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
        Preconditions.checkState(current != null);
        Preconditions.checkArgument(expectedChildren >= 0);

        if (fullyDefined) {
            current.setFullyDefined();
        }
    }

    @Override
    public void startStatement(final int childId, @Nonnull final QName name, final String argument,
            @Nonnull final StatementSourceReference ref) {
        final Optional<StatementContextBase<?, ?, ?>> existing = ctx.lookupDeclaredChild(current, childId);
        current = existing.isPresent() ? existing.get()
                :  Verify.verifyNotNull(ctx.createDeclaredChild(current, childId, name, argument, ref));
    }

    @Override
    public void endStatement(@Nonnull final StatementSourceReference ref) {
        Preconditions.checkState(current != null);
        current.endDeclared(phase);
        exitStatement();
    }

    @Nonnull
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
