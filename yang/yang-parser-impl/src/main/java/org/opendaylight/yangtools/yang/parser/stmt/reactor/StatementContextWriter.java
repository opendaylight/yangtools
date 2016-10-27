/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

final class StatementContextWriter implements StatementWriter {
    private final ModelProcessingPhase phase;
    private final SourceSpecificContext ctx;

    private StatementContextBase<?, ?, ?> parent;
    private ContextBuilder<?, ?, ?> current;

    public StatementContextWriter(final SourceSpecificContext ctx, final ModelProcessingPhase phase) {
        this.ctx = Preconditions.checkNotNull(ctx);
        this.phase = Preconditions.checkNotNull(phase);
    }

    @Override
    public void startStatement(final QName name, final StatementSourceReference ref) {
        deferredCreate();
        current = ctx.createDeclaredChild(parent, name, ref);
    }

    @Override
    public void argumentValue(final String value, final StatementSourceReference ref) {
        Preconditions.checkState(current != null, "Could not set two arguments for one statement: %s", ref);
        current.setArgument(value, ref);
    }

    @Override
    public void endStatement(final StatementSourceReference ref) {
        deferredCreate();
        Preconditions.checkState(parent != null);
        parent.endDeclared(ref,phase);
        parent = parent.getParentContext();
    }

    @Nonnull
    @Override
    public ModelProcessingPhase getPhase() {
        return phase;
    }

    private void deferredCreate() {
        if (current != null) {
            parent = current.build();
            current = null;
        }
    }
}
