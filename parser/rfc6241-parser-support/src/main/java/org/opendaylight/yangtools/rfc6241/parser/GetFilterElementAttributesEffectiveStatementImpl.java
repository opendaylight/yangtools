/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesEffectiveStatement;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesSchemaNode;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class GetFilterElementAttributesEffectiveStatementImpl
        extends AbstractEffectiveUnknownSchmemaNode<Empty, @NonNull GetFilterElementAttributesStatement>
        implements GetFilterElementAttributesEffectiveStatement, GetFilterElementAttributesSchemaNode {
    GetFilterElementAttributesEffectiveStatementImpl(final Current<Empty, GetFilterElementAttributesStatement> stmt,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt.declared(), stmt.getArgument(), stmt.history(), substatements);
    }

    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Override
    public GetFilterElementAttributesEffectiveStatement asEffectiveStatement() {
        return this;
    }
}