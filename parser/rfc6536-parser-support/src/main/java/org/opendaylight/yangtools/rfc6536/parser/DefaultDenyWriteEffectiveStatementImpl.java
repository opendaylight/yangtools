/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteSchemaNode;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class DefaultDenyWriteEffectiveStatementImpl
        extends AbstractEffectiveUnknownSchmemaNode<Empty, @NonNull DefaultDenyWriteStatement>
        implements DefaultDenyWriteEffectiveStatement, DefaultDenyWriteSchemaNode {
    DefaultDenyWriteEffectiveStatementImpl(final Current<Empty, DefaultDenyWriteStatement> stmt,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt.declared(), stmt.getArgument(), stmt.history(), substatements);
    }

    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Override
    public DefaultDenyWriteEffectiveStatement asEffectiveStatement() {
        return this;
    }
}