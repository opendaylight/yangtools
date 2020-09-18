/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DataContainerCompat;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithTypedefNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;

final class StructureEffectiveStatementImpl extends WithTypedefNamespace<QName, @NonNull StructureStatement>
        implements StructureEffectiveStatement, DataNodeContainer.Mixin<StructureEffectiveStatement>,
                   DataContainerCompat<QName, @NonNull StructureStatement> {
    private final @NonNull QName argument;

    StructureEffectiveStatementImpl(final EffectiveStmtCtx.Current<QName, StructureStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(ctx.declared(), substatements);
        argument = ctx.getArgument();
    }

    @Override
    public QName argument() {
        return argument;
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public StructureEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public DataNodeContainer toDataNodeContainer() {
        return this;
    }
}
