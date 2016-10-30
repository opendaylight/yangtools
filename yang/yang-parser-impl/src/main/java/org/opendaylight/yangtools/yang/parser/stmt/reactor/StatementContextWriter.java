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
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

final class StatementContextWriter implements StatementWriter {
    private static final QName TYPE = Rfc6020Mapping.TYPE.getStatementName();

    private final ModelProcessingPhase phase;
    private final SourceSpecificContext ctx;

    private StatementContextBase<?, ?, ?> parent;
    private ContextBuilder<?, ?, ?> current;

    public StatementContextWriter(final SourceSpecificContext ctx, final ModelProcessingPhase phase) {
        this.ctx = Preconditions.checkNotNull(ctx);
        this.phase = Preconditions.checkNotNull(phase);
    }

    @Override
    public void startStatement(final QName name, final String argument, final StatementSourceReference ref) {
        deferredCreate();

        // FIXME: Refactor/clean up this special case
        final QName hackName;
        if (TYPE.equals(name)) {
            SourceException.throwIfNull(argument, ref, "Type statement requires an argument");
            if (TypeUtils.isYangTypeBodyStmtString(argument)) {
                hackName = QName.create(YangConstants.RFC6020_YIN_MODULE, argument);
            } else {
                hackName = QName.create(YangConstants.RFC6020_YIN_MODULE, TYPE.getLocalName());
            }
        } else {
            hackName = name;
        }

        current = ctx.createDeclaredChild(parent, hackName, ref);
        if (argument != null) {
            current.setArgument(argument, ref);
        }
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
