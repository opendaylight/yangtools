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
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.ContextBuilder;

class StatementContextWriter implements StatementWriter {

    private final SourceSpecificContext ctx;
    private StatementContextBase<?, ?, ?> parent;
    private ContextBuilder<?, ?, ?> current;
    private ModelProcessingPhase phase;

    public StatementContextWriter(SourceSpecificContext ctx, ModelProcessingPhase phase) {
        this.ctx = Preconditions.checkNotNull(ctx);
        this.phase = Preconditions.checkNotNull(phase);
    }

    @Override
    public void startStatement(QName name, StatementSourceReference ref) throws SourceException {
        defferedCreate();
        current = ctx.createDeclaredChild(parent, name, ref);

    }

    @Override
    public void argumentValue(String value, StatementSourceReference ref) {
        Preconditions.checkState(current != null, "Could not set two arguments for one statement.");
        current.setArgument(value, ref);
    }

    void defferedCreate() throws SourceException {
        if(current != null) {
            parent = current.build();
            current = null;
        }
    }

    @Override
    public void endStatement(StatementSourceReference ref) throws SourceException {
        defferedCreate();
        Preconditions.checkState(parent != null);
        parent.endDeclared(ref,phase);
        parent = parent.getParentContext();
    }

    @Nonnull
    @Override
    public ModelProcessingPhase getPhase() {
        return phase;
    }

}
