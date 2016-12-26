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
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
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
    public void startStatement(final int childId, @Nonnull final QName name, final String argument,
            @Nonnull final StatementSourceReference ref) {
        current = Verify.verifyNotNull(ctx.createDeclaredChild(current, childId, name, argument, ref));
    }

    @Override
    public void endStatement(@Nonnull final StatementSourceReference ref) {
        Preconditions.checkState(current != null);
        current.endDeclared(ref, phase);
        current = current.getParentContext();
    }

    @Nonnull
    @Override
    public ModelProcessingPhase getPhase() {
        return phase;
    }
}
