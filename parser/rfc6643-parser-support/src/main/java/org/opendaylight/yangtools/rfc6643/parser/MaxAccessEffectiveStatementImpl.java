/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccess;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class MaxAccessEffectiveStatementImpl
        extends AbstractEffectiveUnknownSchmemaNode<MaxAccess, @NonNull MaxAccessStatement>
        implements MaxAccessEffectiveStatement, MaxAccessSchemaNode {
    MaxAccessEffectiveStatementImpl(final Current<MaxAccess, MaxAccessStatement> stmt,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt.declared(), stmt.getArgument(), stmt.history(), substatements);
    }

    @Override
    public MaxAccess getArgument() {
        return argument();
    }

    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Override
    public MaxAccessEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
