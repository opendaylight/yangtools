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
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureArgument;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithSchemaTree;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;

final class AugmentStructureEffectiveStatementImpl
        extends DefaultWithSchemaTree<AugmentStructureArgument, @NonNull AugmentStructureStatement>
        implements AugmentStructureEffectiveStatement {
    private final @NonNull AugmentStructureArgument argument;

    AugmentStructureEffectiveStatementImpl(
            final EffectiveStmtCtx.Current<AugmentStructureArgument, AugmentStructureStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?,?>> substatements) {
        super(stmt.declared(), substatements);
        argument = stmt.getArgument();
    }

    @Override
    public AugmentStructureArgument argument() {
        return argument;
    }

    @Override
    public DataNodeContainer toDataNodeContainer() {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }
}
